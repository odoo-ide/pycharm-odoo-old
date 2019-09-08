import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PySequenceExpression;
import com.jetbrains.python.psi.PyStringLiteralExpression;

import java.util.ArrayList;
import java.util.List;

public class PyExpressionUtils {
    public static List<String> getPySequenceString(PySequenceExpression expression) {
        List<String> result = new ArrayList<>();
        for (PyExpression item : expression.getElements()) {
            if (item instanceof PyStringLiteralExpression) {
                String depend = ((PyStringLiteralExpression) item).getStringValue();
                result.add(depend);
            }
        }
        return result;
    }
}
