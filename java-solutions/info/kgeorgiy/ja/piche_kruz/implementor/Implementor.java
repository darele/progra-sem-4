package info.kgeorgiy.ja.piche_kruz.implementor;

import info.kgeorgiy.java.advanced.implementor.BaseImplementorTest;
import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * Class implementing {@link Impler} generating a default
 * implementation of the specified interface.
 *
 * @author Darwin Piche
 */
public class Implementor implements JarImpler {
    /**
     * Contains File separator char according to operating system
     */
    final char SEP = File.separatorChar;
    /**
     * Tab character.
     */
    private static final String TAB = "\t";
    /**
     * Line Separator character according to platform.
     */
    private final String ENTER = System.lineSeparator();
    /**
     * <code>{</code> Character.
     */
    private final String OPEN_BRACE = "{";
    /**
     * <code>}</code> Character.
     */
    private final String CLOSE_BRACE = "}";
    /**
     * Semicolon character.
     */
    private final String SEMI_COLON = ";";
    /**
     * Space Character.
     */
    private final String SPACE = " ";
    /**
     * Word "Public".
     */
    private final String PUBLIC = "public";

    /**
     * Map containing default return values according to data type.
     */
    private final Map<Type, String> defaultValues =
            Map.of(Boolean.TYPE, "false",
                    Character.TYPE, "'0'",
                    Byte.TYPE, "0",
                    Integer.TYPE, "0",
                    Short.TYPE, "0",
                    Long.TYPE, "0",
                    Float.TYPE, "0",
                    Double.TYPE, "0",
                    Void.TYPE, ""
            );

    /**
     * Private field used to enumerate arguments for a method when
     * generating code for it.
     */
    private int argNum;

    /**
     * Generates relative path to <var>root</var> of the class
     * specified by <var>token</var>.
     *
     * @param token Class for which the relative path will be built.
     * @param root Path relative to which a correct path to
     * <var>token</var> will be created.
     * @return the relative path to the root of <var>token</var>Impl
     * java class implementing <var>token</var> interface.
     */
    protected Path getClassPath(Class<?> token, Path root) {
        StringBuilder newPathName = new StringBuilder(root.toString() + SEP);
        String packageName = getPackageName(token);
        newPathName.append(packageName.replace('.', SEP));
        newPathName.append(SEP).append(token.getSimpleName()).append("Impl.java");
        return Path.of(newPathName.toString());
    }

    /**
     * returns the package's string corresponding to the package
     * holding <var>clazz</var>.
     *
     * @param clazz Class for which package will be extracted.
     * @return Package's name to which <var>clazz</var> belongs, or
     * an empty string if given class belongs to default package.
     */
    protected String getPackageName(Class<?> clazz) {
        Package temp = clazz.getPackage();
        return temp != null ? temp.getName() : "";
    }

    /**
     * Private method for writing first lines of an implementation
     * class.
     *
     * if given class is located in default package, it
     * writes nothing, otherwise, it writes one line of code:
     *  <var>package</var> + name of package to which given class
     * should belong followed by semicolon.
     *
     * @param writer Correct BufferedWriter to which implementation
     * code should be written.
     * @param clazz class instance of interface for which methods
     * should be implemented.
     * @throws IOException If a writing error occurs.
     */
    private void writeHeader(BufferedWriter writer, Class<?> clazz) throws IOException {
        String temp = getPackageName(clazz);
        String ans = !temp.equals("") ? "package " + temp + SEMI_COLON + ENTER : "";
        writer.write(ans);
    }

    /**
     * Private method for writing correct class declaration for
     * the implementation of the given class token to the specified
     * writer.
     *
     * @param writer BufferedWriter to which implementation class
     * declaration should be written.
     * @param token Interface for which implementation is to be created.
     * @throws IOException If a writing error occurs.
     */
    private void writeClassDeclaration(BufferedWriter writer, Class<?> token) throws IOException{
        String IMPLEMENTS = "implements";
        String IMPL = "Impl";
        String CLASS = "class";
        writer.write(PUBLIC + SPACE + CLASS + SPACE +
                token.getSimpleName() + IMPL + SPACE + IMPLEMENTS + SPACE
                + token.getCanonicalName() + SPACE + OPEN_BRACE + ENTER);
    }

    /**
     * Private method to generate implementation of the given interface
     * to the specified writer.
     *
     * This method generates default code for all methods that require
     * implementation, including methods from the interface which
     * we are implementing and methods from interfaces the specified
     * interface extends.
     *
     * @param writer BufferedWriter to which implementation of methods
     * should be written.
     * @param token Interface for which implementation is to be created.
     * @throws IOException If a writing error occurs.
     */
    private void writeImplementations(BufferedWriter writer,
                                      Class<?> token) throws IOException{
        for (Method method : token.getMethods()) {
            if (Modifier.isAbstract(method.getModifiers())) {
                writeMethod(writer, method);
            }
        }
        writer.write(CLOSE_BRACE);
    }

    /**
     * Private method to write implementation of a single method.
     *
     * This is a helping method for
     * {@link #writeImplementations(BufferedWriter, Class)} method, this
     * method correctly generates code for method's declaration,
     * and default body of the given method.
     * @param writer BufferedWriter to which the method's implementation
     * should be written.
     * @param method Method instance to be implemented.
     * @throws IOException If a writing error occurs.
     */
    private void writeMethod(BufferedWriter writer, Method method) throws IOException {
        String OPEN_PARENTHESIS = "(";
        String CLOSE_PARENTHESIS = ")";
        writer.write(TAB + PUBLIC + SPACE + writeType(method.getReturnType()) + method.getName()
                + OPEN_PARENTHESIS + generateMethodArgs(method)
                + CLOSE_PARENTHESIS + SPACE + generateMethodExceptions(method)
                + SPACE + OPEN_BRACE + ENTER + TAB + TAB + generateMethodBody(method)
                + ENTER + TAB + CLOSE_BRACE + ENTER);
    }

    /**
     * Private method for generating code for a single argument for
     * a method.
     *
     * This is a helping method for {@link #generateMethodArgs(Method)} method, which is passed to {@link #typeWriter(Class[], Function)} as predicate to generate code for all arguments of a
     * method as <code>T arg1, T arg2, T arg3,...</code>.
     * @param clazz Type of parameter to be generated.
     * @return A string with argument type, and default argument name.
     * @see #writeType(Class)
     */
    private String produceArg(Class<?> clazz) {
        return writeType(clazz) + "arg" + argNum++;
    }

    /**
     * Private method for generating all arguments of the given method.
     *
     * This is a helping method for {@link #writeMethod(BufferedWriter, Method)} to generate correct method's argument
     * line of code.
     *
     * @param method Method for which code for arguments should be
     * generated.
     * @return A string with all arguments of the specified method.
     */
    private String generateMethodArgs(Method method) {
        argNum = 1;
        return typeWriter(method.getParameterTypes(), this::produceArg);
    }

    /**
     * Private method for generating code for exceptions thrown by
     * the specified method.
     *
     * This method generates a correct line of code that consists of
     * <code>throws</code> and the corresponding exceptions thrown by
     * the specified method, or an empty string if the method
     * does not throw exceptions.
     *
     * @param method The method for which exceptions should be
     * generated.
     * @return A string with the sequence of exceptions thrown by
     * the specified method, of an empty string if the method does not throw any exception.
     */
    private String generateMethodExceptions(Method method) {
        Class<?>[] exceps = method.getExceptionTypes();
        if (exceps.length > 0) {
            return "throws " + typeWriter(exceps, this::writeType);
        }
        return "";
    }

    /**
     * Private method for generating code for an array of classes.
     *
     * This is a helping method for {@link #generateMethodArgs(Method)}
     * and {@link #generateMethodExceptions(Method)}, it takes an array
     * of classes for which code has to be generated, and predicate
     * that specifies how to generate code string for these classes.
     *
     * @param classArr The array of classes for which code should be
     * generated.
     * @param f A predicate that indicates how to generate code for
     * classArr.
     * @return A string with the code sequence for all classes given
     * in <code>classArr</code> argument.
     */
    private String typeWriter(Class<?>[] classArr, Function<Class<?>, String> f) {
        return Arrays.stream(classArr).
                map(f).
                collect(Collectors.joining(", "));
    }

    /**
     * Private method for generating default body for the specified
     * method.
     *
     * This method generates code with a correct return statement for
     * a with default primitive values if it returns a primitive type,
     * null if returns a reference type, or an empty string if it is
     * declared as void.
     *
     * This is a helping method for {@link #writeMethod(BufferedWriter, Method)}.
     *
     * @param method Method for which body has to be generated.
     * @return A string with a correct return statement or an empty
     * string for void methods.
     */
    private String generateMethodBody(Method method) {
        final String RETURN = "return";
        Class<?> returnType = method.getReturnType();
        boolean condition = returnType != Void.TYPE;
        return (condition ? RETURN : "") + SPACE + defaultValues.getOrDefault(returnType, "null")
                + (condition ? SEMI_COLON : "");
    }

    /**
     * Private method for generating code for a data type.
     *
     * This method generates interpretable code line for all class-types.
     *
     * @param clazz Data type for which interpretable code should be
     * generated.
     * @return A string with interpretable java code.
     */
    private String writeType(Class<?> clazz) {
        String BRACKETS = "[]";
        return getType(clazz).getCanonicalName() + (clazz.isArray() ? BRACKETS : "") + " ";
    }

    /**
     * Private method for getting the correct type name for a class.
     *
     * If it is an array class, it returns the type of elements of the
     * array, otherwise it returns the given class parameter type.
     *
     * @param clazz class to get type from.
     * @return The data type of the elements of an array or of the same
     * class if it is not an array.
     */
    private Class<?> getType(Class<?> clazz) {
        if (clazz.isArray()) {
            return clazz.getComponentType();
        }
        return clazz;
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (!token.isInterface() || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException(token.getCanonicalName() + "cannot be implemented");
        }
        Path outputPath = getClassPath(token, root);
        if (outputPath.getParent() != null) {
            try {
                Files.createDirectories(outputPath.getParent());
            } catch (IOException | SecurityException e) {
                //:NOTE: исключения следует пробрасывать, например
                //throw new ImplerException("msg", e)
                //чтобы у кода который вас вызвал была возможность понять в чем причина ошибки
                throw new ImplerException("Output java file hierarchy could not be created\n" + e.getClass().getCanonicalName(), e);
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writeHeader(writer, token);
            writeClassDeclaration(writer, token);
            writeImplementations(writer, token);
        } catch (IOException e) {
            throw new ImplerException("Output class could not be written\n");
        }
    }

    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {

        if (jarFile.getParent() != null) {
            try {
                Files.createDirectories(jarFile.getParent());
            } catch (IOException | SecurityException e) {
                print_error("Could not create java jar file output folder hierarchy\n");
                return;
            }
        }

        Path rootDir = Paths.get(".");

        String classPath = getPackageName(token).replace('.', SEP);

        String className = Path.of(classPath).resolve(token.getSimpleName() + "Impl").toString().replace(File.separatorChar, '/');

        classPath = rootDir.resolve(className).toString();

        implement(token, rootDir);

        List<Charset> charsetList = List.of(
                StandardCharsets.UTF_8,
                StandardCharsets.US_ASCII,
                StandardCharsets.ISO_8859_1,
                StandardCharsets.UTF_16BE,
                StandardCharsets.UTF_16LE,
                StandardCharsets.UTF_16
        );

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        boolean compileFlag = false;

        for (Charset charset : charsetList) {
            try {
                if (compiler.run(null, null, null, "-classpath",
                        rootDir.getFileName() + File.pathSeparator +
                                Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()),
                        rootDir.resolve(className) + ".java",
                        "-encoding", charset.name()) == 0) {
                    compileFlag = true;
                    break;
                }
            } catch (URISyntaxException e) {
                throw new ImplerException("URISyntaxException", e);
            }
        }

        if (!compileFlag) {
            throw new ImplerException("Failed to compile files");
        }

        List<String> files = new ArrayList<>();
        files.add(getClassPath(token, rootDir).toString());

        BaseImplementorTest.compileFiles(rootDir, files);

        try(JarOutputStream writer = new JarOutputStream(Files.newOutputStream(jarFile))){
            writer.putNextEntry(new ZipEntry(className + ".class"));
            Files.copy(Path.of(classPath + ".class"), writer);
        } catch (IOException e) {
            throw new ImplerException("Jar file " + jarFile + " Could not be created\n" + e.getMessage());
        }
    }

    /**
     * Private method used for error output to standard error stream.
     *
     * @param message - Message to be written to standard error stream.
     */
    private static void print_error(String message) {
        System.err.println(message);
    }

    /**
     * Entry point for this class.
     *
     * To implement an interface and generate the corresponding
     * jar file, three arguments have to be given.
     *
     * <ol>
     *  <li> <code>-jar</code></li>
     *  <li> Interface's canonical name for which implementation
     *       will be generated </li>
     *  <li> Output file name for generated jar file </li>
     * </ol>
     *
     * To generate only interface's implementation, only second and
     * third argument have to be given (2 arguments in total), instead
     * of jar file's path, root directory for implemented interface
     * has to be specified as requested by {@link #implement(Class, Path)}.
     *
     * @param args list of parameters to be given to our program (
     * expected 2 or 3 arguments).
     */
    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            print_error("Expected 2 or 3 arguments, " + args.length + " given\n");
            return;
        } else if (args.length == 3 && !args[0].equals("-jar")) {
            print_error("Unknown option. First argument expected to be -jar\n");
            return;
        }

        for (String s : args) {
            if (s == null) {
                print_error("No null arguments required\n");
                return;
            }
        }

        String jarFile;
        String className;

        if (args.length == 2) {
            className = args[0];
            jarFile = args[1];
        } else {
            className = args[1];
            jarFile = args[2];
        }

        final Class<?> token;
        final Path jarFilePath;

        try {
            token = Class.forName(className);
            jarFilePath = Path.of(jarFile);
        } catch (ClassNotFoundException e) {
            print_error("given class name could not be found, " + e.getMessage());
            return;
        } catch (InvalidPathException e) {
            print_error("Output path not found " + e.getMessage());
            return;
        }

        Implementor a = new Implementor();

        try {
            if (args.length == 2) {
                a.implement(token, jarFilePath);
            } else {
                a.implementJar(token, jarFilePath);
            }
        } catch (ImplerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Default constructor, for invocation from extended classes
     * */
    public Implementor(){

    }
}

