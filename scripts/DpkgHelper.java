import java.io.*;

/**
 * Helper to enable image scanning on Quay Claire.
 * Image scanner expects /var/lib/dpkg/status instead of /var/lib/dpkg/status.d/
 * Generates the file from directory content so Clair can work on it.
 * Needs to be a java script, as it is run on gcr.io/distroless/java:11, which has no bash.
 */
class DpkgHelper {
  public static void main(String[] args) throws IOException {
      File dir = new File("/var/lib/dpkg/status.d/");
      PrintWriter pw = new PrintWriter("/var/lib/dpkg/status");
      String[] fileNames = dir.list();

      for (String fileName : fileNames) {
        System.out.println("Handling file: " + fileName);
        File f = new File(dir, fileName);
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line = br.readLine();
        while (line != null) {
          pw.println(line);
          line = br.readLine();
        }
      pw.println();
      pw.flush();
    }
  }
}
