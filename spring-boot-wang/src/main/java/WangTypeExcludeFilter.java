import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class WangTypeExcludeFilter extends TypeExcludeFilter {

	@Override
	public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
		return metadataReader.getAnnotationMetadata().getClassName()
				.equals("com.wang.springboot.service.UserService");
	}
}
