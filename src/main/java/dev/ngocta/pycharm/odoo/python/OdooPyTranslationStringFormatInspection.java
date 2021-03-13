package dev.ngocta.pycharm.odoo.python;

import com.google.common.collect.ImmutableMap;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.PyPsiBundle;
import com.jetbrains.python.PyStringFormatParser;
import com.jetbrains.python.inspections.PyInspection;
import com.jetbrains.python.inspections.PyInspectionVisitor;
import com.jetbrains.python.inspections.quickfix.PyAddSpecifierToFormatQuickFix;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.types.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jetbrains.python.PyStringFormatParser.filterSubstitutions;
import static com.jetbrains.python.PyStringFormatParser.parsePercentFormat;
import static com.jetbrains.python.psi.PyUtil.as;

public class OdooPyTranslationStringFormatInspection extends PyInspection {
    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder,
                                          boolean isOnTheFly,
                                          @NotNull LocalInspectionToolSession session) {
        return new Visitor(holder, session);
    }

    @Override
    @NotNull
    public String getShortName() {
        return "OdooPyTranslationStringFormat";
    }

    public static class Visitor extends PyInspectionVisitor {
        private static class Inspection {
            private static final ImmutableMap<Character, String> PERCENT_FORMAT_CONVERSIONS = ImmutableMap.<Character, String>builder()
                    .put('d', "int or long or float")
                    .put('i', "int or long or float")
                    .put('o', "int or long or float")
                    .put('u', "int or long or float")
                    .put('x', "int or long or float")
                    .put('X', "int or long or float")
                    .put('e', "float")
                    .put('E', "float")
                    .put('f', "float")
                    .put('F', "float")
                    .put('g', "float")
                    .put('G', "float")
                    .put('c', "str")
                    .put('r', "str")
                    .put('a', "str")
                    .put('s', "str")
                    .put('b', "bytes")
                    .build();

            private final Map<String, Boolean> myUsedMappingKeys = new HashMap<>();
            private int myExpectedArguments = 0;
            private boolean myProblemRegister = false;
            private final Visitor myVisitor;
            private final TypeEvalContext myTypeEvalContext;

            Inspection(Visitor visitor, TypeEvalContext typeEvalContext) {
                myVisitor = visitor;
                myTypeEvalContext = typeEvalContext;
            }

            // return number of arguments or -1 if it can not be computed
            private int inspectArguments(@Nullable final PyExpression rightExpression,
                                         @NotNull final PsiElement problemTarget) {
                if (rightExpression instanceof PyTupleExpression) {
                    return ((PyTupleExpression) rightExpression).getElements().length;
                }
                return 1;
            }

            private void registerProblem(@NotNull PsiElement problemTarget,
                                         @NotNull String message,
                                         @NotNull LocalQuickFix quickFix) {
                myProblemRegister = true;
                myVisitor.registerProblem(problemTarget, message, quickFix);
            }

            private void registerProblem(@NotNull PsiElement problemTarget,
                                         @NotNull String message) {
                myProblemRegister = true;
                myVisitor.registerProblem(problemTarget, message);
            }

            private void inspectPercentFormat(@NotNull final PyStringLiteralExpression formatExpression) {
                final String value = formatExpression.getText();
                final List<PyStringFormatParser.SubstitutionChunk> chunks = filterSubstitutions(parsePercentFormat(value));

                myExpectedArguments = chunks.size();
                myUsedMappingKeys.clear();

                // if use mapping keys
                final boolean mapping = chunks.size() > 0 && chunks.get(0).getMappingKey() != null;
                for (int i = 0; i < chunks.size(); ++i) {
                    PyStringFormatParser.PercentSubstitutionChunk chunk = as(chunks.get(i), PyStringFormatParser.PercentSubstitutionChunk.class);
                    if (chunk != null) {
                        // Mapping key
                        String mappingKey = Integer.toString(i + 1);
                        if (mapping) {
                            if (chunk.getMappingKey() == null || chunk.isUnclosedMapping()) {
                                registerProblem(formatExpression, PyPsiBundle.message("INSP.too.few.keys"));
                                break;
                            }
                            mappingKey = chunk.getMappingKey();
                            myUsedMappingKeys.put(mappingKey, false);
                        }

                        // Minimum field width
                        inspectWidth(formatExpression, chunk.getWidth());

                        // Precision
                        inspectWidth(formatExpression, chunk.getPrecision());

                        // Format specifier
                        final char conversionType = chunk.getConversionType();
                        if (conversionType == 'b') {
                            final LanguageLevel languageLevel = LanguageLevel.forElement(formatExpression);
                            if (languageLevel.isOlderThan(LanguageLevel.PYTHON35) || !isBytesLiteral(formatExpression, myTypeEvalContext)) {
                                registerProblem(formatExpression, PyPsiBundle.message("INSP.str.format.unsupported.format.character.b"));
                                return;
                            }
                        }
                        final LanguageLevel languageLevel = LanguageLevel.forElement(formatExpression);
                        if (PERCENT_FORMAT_CONVERSIONS.containsKey(conversionType) && !(languageLevel.isPython2() && conversionType == 'a')) {
                            continue;
                        }
                        registerProblem(formatExpression, PyPsiBundle.message("INSP.no.format.specifier.char"), new PyAddSpecifierToFormatQuickFix());
                        return;
                    }
                }
            }

            private static boolean isBytesLiteral(@NotNull PyStringLiteralExpression expr,
                                                  @NotNull TypeEvalContext context) {
                final PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(expr);
                final PyClassType bytesType = builtinCache.getBytesType(LanguageLevel.forElement(expr));
                final PyType actualType = context.getType(expr);
                return bytesType != null && actualType != null && PyTypeChecker.match(bytesType, actualType, context);
            }

            private void inspectWidth(@NotNull final PyStringLiteralExpression formatExpression,
                                      String width) {
                if ("*".equals(width)) {
                    ++myExpectedArguments;
                    if (myUsedMappingKeys.size() > 0) {
                        registerProblem(formatExpression, PyPsiBundle.message("INSP.str.format.can.not.use.star.in.formats.when.using.mapping"));
                    }
                }
            }

            public boolean isProblem() {
                return myProblemRegister;
            }

            private void inspectValues(@Nullable final PyExpression rightExpression) {
                if (rightExpression == null) {
                    return;
                }
                if (rightExpression instanceof PyParenthesizedExpression) {
                    inspectValues(((PyParenthesizedExpression) rightExpression).getContainedExpression());
                } else {
                    final PyClassType type = as(myTypeEvalContext.getType(rightExpression), PyClassType.class);
                    if (type != null) {
                        if (myUsedMappingKeys.size() > 0 && !PyABCUtil.isSubclass(type.getPyClass(), PyNames.MAPPING, myTypeEvalContext)) {
                            registerProblem(rightExpression, PyPsiBundle.message("INSP.format.requires.mapping"));
                            return;
                        }
                    }
                    inspectArgumentsNumber(rightExpression);
                }
            }

            private void inspectArgumentsNumber(@NotNull final PyExpression rightExpression) {
                final int arguments = inspectArguments(rightExpression, rightExpression);
                if (myUsedMappingKeys.isEmpty() && arguments >= 0) {
                    if (myExpectedArguments < arguments) {
                        registerProblem(rightExpression, PyPsiBundle.message("INSP.too.many.args.for.fmt.string"));
                    } else if (myExpectedArguments > arguments) {
                        registerProblem(rightExpression, PyPsiBundle.message("INSP.too.few.args.for.fmt.string"));
                    }
                }
            }
        }

        public Visitor(ProblemsHolder holder,
                       LocalInspectionToolSession session) {
            super(holder, session);
        }

        @Override
        public void visitPyBinaryExpression(@NotNull PyBinaryExpression node) {
            if (OdooPyUtils.isTranslationStringExpression(node.getLeftExpression()) && node.isOperator("%")) {
                Inspection inspection = new Inspection(this, myTypeEvalContext);
                PyStringLiteralExpression literalExpression = PsiTreeUtil.findChildOfType(node.getLeftExpression(), PyStringLiteralExpression.class);
                if (literalExpression != null) {
                    inspection.inspectPercentFormat(literalExpression);
                    if (inspection.isProblem()) {
                        return;
                    }
                    inspection.inspectValues(node.getRightExpression());
                }
            }
        }
    }
}