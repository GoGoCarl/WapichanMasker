import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class Wapichan {

	public static void main(String[] args) throws Exception {
		File sourceFolder = null, targetFolder = null;
		
		List<String> included = new ArrayList<String>();
		included.add(".htm");
		
		List<String> excluded = new ArrayList<String>();
		
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				if ("-u".equals(args[0]) || "--usage".equals(args[0]) || "--help".equals(args[0])) {
					File cwd = new File(System.getProperty("user.dir"));
					File td = new File(cwd.getParentFile(), "WapichanSiteMasked");
					System.out.println("Arguments:");
					System.out.println("-------------------");
					System.out.println("-s {path} -- source directory (default " + cwd + ")");
					System.out.println("-t {path} -- target directory (default " + td + ")");
					System.out.println("-e {extension} -- include extensions (default .htm)");
					System.out.println("-x {extension} -- exclude extensions");
					System.exit(0);
				}
			}
			for (int i = 0; i < args.length; i++) {
				if (args.length >= i + 2) {
					if ("-s".equals(args[i]))
						sourceFolder = new File(args[i+1]);
					else if ("-t".equals(args[i]))
						targetFolder = new File(args[i+1]);
					else if ("-e".equals(args[i])) {
						for (String extension : args[i+1].split(" "))
							included.add(extension.trim());
					} else if ("-x".equals(args[i])) {
						for (String extension : args[i+1].split(" "))
							excluded.add(extension.trim());
					}
				}
			}
		}
		
		if (sourceFolder == null) {
			String workingDir = System.getProperty("user.dir");
			sourceFolder = new File(workingDir);
		}
		
		if (targetFolder == null) {
			targetFolder = new File(sourceFolder.getParentFile(), "WapichanSiteMasked");
		}
		
		if (!sourceFolder.exists()) {
			System.out.println("Error: Source folder doesn't exist: " + sourceFolder);
			System.exit(1);
		}
		
		System.out.println("=========================================");

		System.out.println("Reading from " + sourceFolder);
		System.out.println("Writing to " + targetFolder);
		System.out.println("Including extensions:");
		for (String ext : included) {
			System.out.println(" * " + ext);
		}
		if (!excluded.isEmpty()) {
			System.out.println("Excluding extensions:");
			for (String ext : excluded) {
				System.out.println(" * " + ext);
			}
		}
		
		System.out.println("=========================================");
		
		targetFolder.mkdirs();
		
		WapichanConverter converter = new WapichanConverter(sourceFolder, targetFolder);
		
		for (String file : sourceFolder.list()) {
			if (hasExtension(file, excluded)) {
				System.out.println(" x Skipping " + file);
				continue;
			}
			else if (hasExtension(file, included)) {
				converter.mask(file);
				System.out.println(" + Edited " + file);
			} else {
				File sourceFile = new File(sourceFolder, file);
				File targetFile = new File(targetFolder, file);
				if (sourceFile.isDirectory())
					copyDirectory(sourceFile, targetFile);
				else
					copyFile(sourceFile, targetFile);
				System.out.println(" - Copied " + file);
			}
		}
	}
	
	private static boolean hasExtension(String file, List<String> extensions) {
		for (String ext : extensions) {
			if (file.endsWith(ext))
				return true;
		}
		return false;
	}
	
	private static final void copyDirectory(File source, File destination) throws IOException {
		if (!source.isDirectory()) {
			throw new IllegalArgumentException("Source (" + source.getPath()
					+ ") must be a directory.");
		}

		if (!source.exists()) {
			throw new IllegalArgumentException("Source directory ("
					+ source.getPath() + ") doesn't exist.");
		}

		/*if (destination.exists()) {
			throw new IllegalArgumentException("Destination (" + destination.getPath() + ") exists.");
		}*/

		destination.mkdirs();
		File[] files = source.listFiles();

		for (File file : files) {
			if (file.isDirectory()) {
				copyDirectory(file, new File(destination, file.getName()));
			} else {
				copyFile(file, new File(destination, file.getName()));
			}
		}
	}

	private static final void copyFile(File source, File destination)
			throws IOException {
		FileChannel sourceChannel = new FileInputStream(source).getChannel();
		FileChannel targetChannel = new FileOutputStream(destination)
				.getChannel();
		sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
		sourceChannel.close();
		targetChannel.close();
	}

}
