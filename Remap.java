import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

/**
 * Applies upstream Scholar commit eb775040 ("Compat with newest JEI", 1.21.1 branch) to a
 * Scholar 26.1.2 jar: JEI 29.43 moved UserInput and IUserInputHandler from mezz.jei.gui.input to
 * mezz.jei.common.input, so JeiKeyConflictContextMixin's handler descriptor no longer matched and
 * the mixin failed (taking JEI's GlobalInputHandler with it). Remaps those two class names in
 * that one class; every other jar entry is copied unchanged.
 *
 * Usage: java Remap <in.jar> <out.jar>
 */
public class Remap {
    static final String ENTRY = "io/github/mortuusars/scholar/mixin/jei/JeiKeyConflictContextMixin.class";
    static final Map<String, String> MAP = Map.of(
        "mezz/jei/gui/input/UserInput", "mezz/jei/common/input/UserInput",
        "mezz/jei/gui/input/IUserInputHandler", "mezz/jei/common/input/IUserInputHandler");

    public static void main(String[] args) throws IOException {
        if (args.length != 2) { System.err.println("usage: java Remap <in.jar> <out.jar>"); System.exit(1); }
        boolean found = false;
        try (ZipFile in = new ZipFile(args[0]);
             ZipOutputStream out = new ZipOutputStream(new FileOutputStream(args[1]))) {
            for (Enumeration<? extends ZipEntry> e = in.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = e.nextElement();
                byte[] data = in.getInputStream(entry).readAllBytes();
                if (entry.getName().equals(ENTRY)) {
                    String before = new String(data, "ISO-8859-1");
                    if (!before.contains("mezz/jei/gui/input/UserInput")) {
                        System.err.println("already patched or unexpected class: " + ENTRY); System.exit(2);
                    }
                    ClassReader reader = new ClassReader(data);
                    ClassWriter writer = new ClassWriter(0);
                    reader.accept(new ClassRemapper(writer, new SimpleRemapper(MAP)), 0);
                    data = writer.toByteArray();
                    found = true;
                }
                ZipEntry copy = new ZipEntry(entry.getName());
                copy.setTime(entry.getTime());
                out.putNextEntry(copy);
                out.write(data);
                out.closeEntry();
            }
        }
        if (!found) { System.err.println(ENTRY + " not found"); System.exit(3); }
        System.out.println("patched " + ENTRY + " -> " + args[1]);
    }
}
