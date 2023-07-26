import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Opcodes;

import java.io.IOException;

public class ASMTest {

	public static void main(String[] args) {
		try {
			ClassReader reader = new ClassReader("com.zhouyu.service.UserService");

			System.out.println(reader.getClassName());

			ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM7) {
				@Override
				public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
					System.out.println(descriptor);
					return super.visitAnnotation(descriptor, visible);
				}
			};

			reader.accept(classVisitor, ClassReader.SKIP_DEBUG);


		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
