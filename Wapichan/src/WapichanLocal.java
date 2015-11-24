import java.io.File;


@SuppressWarnings("unused")
public class WapichanLocal {
	
	private static final File sourceFolder = new File("/Users/carlscott/Projects/FPP/WapichanSite");
	private static final File targetFolder = new File("/Users/carlscott/Projects/FPP/WapichanSiteMasked");
	
	
	public static void main(String[] args) throws Exception {
		writeAbout();
		//writeAll();
	}

	private static void writeAll() throws Exception {
		Wapichan.main(new String[] { "-s", "/Users/carlscott/Projects/FPP/WapichanSite" });
	}
	
	private static void writeAbout() throws Exception {
		WapichanConverter converter = new WapichanConverter(sourceFolder, targetFolder);
		converter.setWriteToFile(false);
		converter.mask("about.htm");
	}

}
