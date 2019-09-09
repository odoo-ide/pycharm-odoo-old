import com.intellij.util.io.DataExternalizer;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OdooModuleDataExternalizer implements DataExternalizer<OdooModuleInfo> {
    public static final OdooModuleDataExternalizer INSTANCE = new OdooModuleDataExternalizer();

    @Override
    public void save(@NotNull DataOutput out, OdooModuleInfo value) throws IOException {
        List<String> depends = value.getDepends();
        out.writeInt(depends.size());
        for (String s : depends) {
            out.writeUTF(s);
        }
    }

    @Override
    public OdooModuleInfo read(@NotNull DataInput in) throws IOException {
        ArrayList<String> depends = new ArrayList<>();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String s = in.readUTF();
            depends.add(s);
        }
        return new OdooModuleInfo(depends);
    }
}
