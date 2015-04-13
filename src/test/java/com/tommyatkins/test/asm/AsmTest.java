package com.tommyatkins.test.asm;

import java.io.File;
import java.io.FileOutputStream;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.tommyatkins.test.asm.output.Result;

public class AsmTest {
	public static void main(String[] args) throws Exception {
//		addMethod();
		// new Result().test();
		System.out.println(Type.getType(String.class));;
	}

	public static void addMethod() throws Exception {
		ClassReader reader = new ClassReader(Result.class.getName());
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		Adapter adapter = new Adapter(writer);
		reader.accept(adapter, 0);

		MethodVisitor test = writer.visitMethod(Opcodes.ACC_PUBLIC, "test", "()V", null, null);
		test.visitCode();

		test.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		test.visitLdcInsn("Before execute");
		test.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");

		test.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		test.visitLdcInsn("End execute");
		test.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");

		test.visitInsn(Opcodes.RETURN);
		test.visitMaxs(0, 0);

		test.visitEnd();

		byte[] data = writer.toByteArray();

		String path = ClassLoader.getSystemResource(Result.class.getName().replaceAll("[\\.]", "/") + ".class").getFile();
		File out = new File(path);
		FileOutputStream fos = new FileOutputStream(out);
		fos.write(data);

		fos.close();

	}
}

class Loader extends ClassLoader {

	@SuppressWarnings("unchecked")
	public <T> Class<T> loadByte(Class<T> tc, byte[] b) {
		return (Class<T>) defineClass(tc.getName(), b, 0, b.length);
	}

	public Class<?> loadByte(String name, byte[] b) {
		return defineClass(name, b, 0, b.length);
	}

}

class Adapter extends ClassAdapter {

	public Adapter(ClassVisitor cv) {
		super(cv);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		cv.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		return cv.visitField(access, name, desc, signature, value);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if ("test".equals(name)) {
			return new CopyAllMethodAdapter(cv.visitMethod(access, "copy", desc, signature, exceptions));
		} else if ("copy".equals(name)) {
			return null;
		}
		return cv.visitMethod(access, name, desc, signature, exceptions);
	}

}

class CopyAllMethodAdapter extends MethodAdapter {

	public CopyAllMethodAdapter(MethodVisitor ma) {
		super(ma);
	}

}