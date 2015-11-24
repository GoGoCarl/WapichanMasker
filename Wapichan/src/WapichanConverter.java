import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WapichanConverter {
	
	private final File sourceFolder, targetFolder;
	private final Map<String, String> replacements = new HashMap<String, String>();
	
	private boolean writeToFile = true;
	
	public WapichanConverter(File sourceFolder, File targetFolder) {
		this.sourceFolder = sourceFolder;
		this.targetFolder = targetFolder;
		
		replacements.put("Wapichan".toLowerCase(), "FECONAU");
		replacements.put("About".toLowerCase(), "Nosotros");
		replacements.put("Villages".toLowerCase(), "Comunidades");
		replacements.put("News".toLowerCase(), "Noticias");
		replacements.put("Products".toLowerCase(), "Productos");
		replacements.put("Interactive Map".toLowerCase(), "Mapas Documentos");
	}
	
	public void setWriteToFile(boolean writeToFile) {
		this.writeToFile = writeToFile;
	}

	public void mask(String file) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(new File(sourceFolder, file)));
		BufferedWriter writer;
		if (writeToFile) {
			File targetFile = new File(targetFolder, file);
			targetFile.getParentFile().mkdirs();
			
			writer = new BufferedWriter(new PrintWriter(new FileWriter(targetFile)));
		} else {
			writer = new BufferedWriter(new OutputStreamWriter(new OpenOutputStream()));
		}

		String line = null;
		List<String> ignored = Arrays.asList("link", "meta", "title", "nav",
				"script");
		String waitingForKey = null;

		while ((line = reader.readLine()) != null) {
			if (waitingForKey == null) {
				for (String key : ignored) {
					if (line.matches("^\\s*" + "<" + key + ".*?"))
						waitingForKey = key;
				}
			}

			if (waitingForKey != null) {
				if ("title".equals(waitingForKey)) {
					Matcher matcher = Pattern.compile(
							"(.*<title>)(.*?)(</title>.*)").matcher(line);
					if (matcher.find()) {
						writer.write(line.replaceAll(matcher.group(2),
								replaceNav(matcher.group(2))));
						// writer.write(Replacer.replace(line, matcher.group(2),
						// replaceNav(matcher.group(2))));
					} else
						writer.write(line);
					writer.write("\n");
					waitingForKey = null;
					continue;
				} else if (line.matches("^\\s*.*" + "</" + waitingForKey
						+ ".*?")) {
					writer.write(line);
					writer.write("\n");
					waitingForKey = null;
					continue;
				} else if (line.matches("^\\s*" + "<" + waitingForKey
						+ ".*/>.*")) {
					writer.write(line);
					writer.write("\n");
					waitingForKey = null;
					continue;
				}
			}

			if (waitingForKey == null) {
				// not waiting, process line
				if (line.matches("^\\s*<.*")) {
					// This will fix the page title for most pages
					if (line.matches("^\\s*" + "<.*?>.+</.*?>.*"))
						writer.write(markOut(line));
					else if (line.matches("^\\s*" + "<.*>.+"))
						writer.write(markOut(line));
					else
						writer.write(line);
				} else
					writer.write(markOut(line));
			} else if ("nav".equals(waitingForKey)) {
				if (line.matches("^\\s*.*<a.*>.+</a>.*")) {
					Matcher matcher = Pattern.compile(
							"(^\\s*.*<a.*?>)(.*)(</a>)(.*)").matcher(line);
					if (matcher.find()) {
						writer.write(line.replaceAll(matcher.group(2),
								replaceNav(matcher.group(2))));
						// writer.write(Replacer.replace(line, matcher.group(2),
						// replaceNav(matcher.group(2))));
					} else
						writer.write(line);
				} else
					writer.write(line);
			} else {
				writer.write(line);
			}

			writer.write("\n");
		}

		reader.close();
		writer.close();
	}

	private String replaceNav(String value) {
		if (value == null)
			return value;

		StringBuilder kb = new StringBuilder();
		StringBuilder rem = null;
		for (char c : value.toLowerCase().toCharArray()) {
			if (c == '<')
				rem = new StringBuilder();

			if (rem == null)
				kb.append(c);
			else
				rem.append(c);
		}
		String key = kb.toString().trim();
		if (replacements.containsKey(key))
			return replacements.get(key) + (rem == null ? "" : rem.toString());
		else
			return value;
	}

	private String markOut(String line) {
		StringBuilder out = new StringBuilder();
		boolean change = true;
		for (char c : line.toCharArray()) {
			if (c == '<') {
				out.append(c);
				change = false;
			} else if (c == '>') {
				out.append(c);
				change = true;
			} else if (Character.isWhitespace(c)) {
				out.append(c);
			} else if (change) {
				if (Character.isUpperCase(c))
					out.append('X');
				else
					out.append('x');
			} else {
				out.append(c);
			}
		}
		return out.toString();
	}
	
	class OpenOutputStream extends PrintStream {
		
		public OpenOutputStream() {
			super(System.out);
		}
		
		@Override
		public void close() {
			// Nope
		}
		
	}

}
