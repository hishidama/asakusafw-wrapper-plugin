package jp.hishidama.asakusafw_wrapper.dmdl;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import jp.hishidama.asakusafw_wrapper.dmdl.DmdlSourceUri.Info;
import jp.hishidama.asakusafw_wrapper.dmdl.excel.TestSheetGenerator;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Region;
import com.asakusafw.dmdl.analyzer.DmdlAnalyzer;
import com.asakusafw.dmdl.analyzer.DmdlSemanticException;
import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.model.AstScript;
import com.asakusafw.dmdl.parser.DmdlParser;
import com.asakusafw.dmdl.parser.DmdlSyntaxException;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.source.DmdlSourceRepository;
import com.asakusafw.dmdl.source.DmdlSourceRepository.Cursor;
import com.asakusafw.dmdl.spi.AttributeDriver;
import com.asakusafw.dmdl.spi.TypeDriver;

public class DmdlParserCaller {

	public List<Object[]> parse(List<Object[]> files) throws IOException {
		List<Object[]> result = new ArrayList<Object[]>();
		analyze(files, result);
		return result;
	}

	public DmdlSemantics analyze(List<Object[]> files, List<Object[]> result) throws IOException {
		List<Info> list = new ArrayList<Info>(files.size());
		for (Object[] arr : files) {
			list.add(new Info((URI) arr[0], (String) arr[1]));
		}
		DmdlSourceRepository repository = new DmdlSourceUri(list);
		DmdlAnalyzer analyzer = parse(repository, result);
		try {
			return analyzer.resolve();
		} catch (DmdlSemanticException e) {
			for (Diagnostic diagnostic : e.getDiagnostics()) {
				int level;
				switch (diagnostic.level) {
				case INFO:
					level = 0; // IMarker.SEVERITY_INFO
					break;
				case WARN:
					level = 1; // IMarker.SEVERITY_WARNING
					break;
				default:
					level = 2; // IMarker.SEVERITY_ERROR
					break;
				}
				result.add(createResult(level, diagnostic.message, diagnostic.region));
			}
			return null;
		}
	}

	// see com.asakusafw.dmdl.util.AnalyzeTask
	protected DmdlAnalyzer parse(DmdlSourceRepository source, List<Object[]> result) throws IOException {
		DmdlParser parser = new DmdlParser();

		ClassLoader loader = getClass().getClassLoader();
		DmdlAnalyzer analyzer = new DmdlAnalyzer(ServiceLoader.load(TypeDriver.class, loader), ServiceLoader.load(
				AttributeDriver.class, loader));
		Cursor cursor = source.createCursor();
		try {
			while (cursor.next()) {
				URI name = cursor.getIdentifier();
				Reader resource = cursor.openResource();
				try {
					AstScript script = parser.parse(resource, name);
					for (AstModelDefinition<?> model : script.models) {
						analyzer.addModel(model);
					}
				} catch (DmdlSyntaxException e) {
					result.add(createResult(2, e.getMessage(), e.getRegion()));
				} finally {
					resource.close();
				}
			}
		} finally {
			cursor.close();
		}
		return analyzer;
	}

	protected Object[] createResult(int level, String message, Region region) {
		if (region == null) {
			return new Object[] { null, level, message, -1, 0, -1, 0 };
		}
		return new Object[] { region.sourceFile, level, message, region.beginLine, region.beginColumn, region.endLine,
				region.endColumn };
	}

	public void generateTestSheet(String version, List<Object[]> files, List<String[]> names) throws Exception {
		List<Object[]> result = new ArrayList<Object[]>();
		DmdlSemantics dmdlSemantics = analyze(files, result);

		String className = String.format("%s%s", TestSheetGenerator.class.getName(), version);
		@SuppressWarnings("unchecked")
		Class<? extends TestSheetGenerator> clazz = (Class<? extends TestSheetGenerator>) Class.forName(className);
		TestSheetGenerator generator = clazz.newInstance();
		generator.execute(dmdlSemantics, names);
	}
}
