import java.io.*;

public class writeTouchStone
{

  public writeTouchStone()
  {
  }

  public void write1p(double[] freq_mhz, double[] data_real, double[] data_imag)
  {
    try {
      File file = new File("./nanovna.s1p");
      PrintWriter pw = new PrintWriter(file);

      pw.println("! NanoVNA S11 output"); 
      pw.println("! Frequency       S11");
      pw.println("# MHz  S  RI R  50");

      for(int i=0; i<freq_mhz.length; i++) {
        pw.println( String.format("%3.3f   %3.3f   %3.3f", new Double(freq_mhz[i]).doubleValue()/1e6, data_imag[i], data_real[i]) );
      }

      pw.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

}
