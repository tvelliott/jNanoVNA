
/*
MIT License

Copyright (c) 2019 tvelliott

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

*/

import java.io.*;

//////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
public class writeTouchStone
{

//////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
  public writeTouchStone()
  {
  }

//////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
  public void write1p(double[] freq_mhz, double[] data_real, double[] data_imag)
  {
    try {
      File file = new File("./nanovna.s1p");
      PrintWriter pw = new PrintWriter(file);

      pw.println("! NanoVNA S11 output"); 
      pw.println("! Frequency       S11");
      pw.println("# Hz  S  RI R  50");

      for(int i=0; i<freq_mhz.length; i++) {
        pw.println( String.format("%3.6f   %3.6f   %3.6f", new Double(freq_mhz[i]).doubleValue(), data_real[i], data_imag[i]) );
      }

      pw.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  public void write2p(double[] freq_mhz, double[] data_real_s11, double[] data_imag_s11, double[] data_real_s21, double[] data_imag_s21)
  {
    try {
      File file = new File("./nanovna.s2p");
      PrintWriter pw = new PrintWriter(file);

      pw.println("! NanoVNA S11/S21 output"); 
      pw.println("! Frequency       S11                S21                S12                S22");
      pw.println("# Hz  S  RI R  50");

      for(int i=0; i<freq_mhz.length; i++) {
        pw.println( String.format("%3.6f   %3.6f   %3.6f   %3.6f   %3.6f   %3.6f   %3.6f   %3.6f   %3.6f", new Double(freq_mhz[i]).doubleValue(),
            data_real_s11[i], data_imag_s11[i], data_real_s21[i], data_imag_s21[i], 
            data_real_s11[i], data_imag_s11[i], data_real_s21[i], data_imag_s21[i])
          );
      }

      pw.close();
    } catch(Exception e) {
    }
  }

}
