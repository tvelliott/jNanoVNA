
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
import com.fazecast.jSerialComm.*;
import java.io.*;
import java.lang.*;
import java.util.*;

///////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////
public class VNAFrame extends javax.swing.JFrame
{

  SerialPort nanoVNA_port;
  java.util.Timer utimer;
  smithChartPanel smith_panel;
  writeTouchStone touchstone;
  magPanel mag_plot;
  int plot_count=0;
  private javax.swing.JLabel count_label;

  double[] real_s11;
  double[] imag_s11;
  double[] real_s21;
  double[] imag_s21;

  /////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////
  class updateTask extends java.util.TimerTask
  {

    public void run()
    {
      try {
        if(sweep_toggle.isSelected()) {
          waitPrompt();
          //do sweep to update plots
          if(do_plot_s11.isSelected()) getData(0); //S11
          if(do_plot_s21.isSelected()) getData(1); //S21
        }
      } catch(Exception e) {
        e.printStackTrace(System.out);
      }
    }
  }
  //////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////
  public void waitPrompt()
  {
    byte[] data_cmd = new String("\r\n").getBytes();
    int len = nanoVNA_port.writeBytes( data_cmd, data_cmd.length, 0);
    byte[] data_buffer = new byte[256];
    int i=0;

    while(true) {
      len = nanoVNA_port.readBytes( data_buffer, 256, 0);

      //look for prompt from nanoVNA
      if(len>0 && new String(data_buffer).contains("\r\nch>") ) break;

      try {
        Thread.sleep(50);
      } catch(Exception e) {
        e.printStackTrace();
      }
      len = nanoVNA_port.writeBytes( data_cmd, data_cmd.length, 0);

      if(i++%20==0) System.out.println("\r\nwaiting for ch> prompt from NanoVNA");

      try {
        Thread.sleep(50);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    try {
      Thread.sleep(50);
    } catch(Exception e) {
      e.printStackTrace();
    }

  }

  //////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////
  public void getData(int data_type)
  {
    byte[] data_cmd = new String("data "+data_type+"\r\n").getBytes();

    int len = nanoVNA_port.writeBytes( data_cmd, data_cmd.length, 0);
    byte[] data_buffer = new byte[64000];

    try {
      Thread.sleep(1500);
    } catch(Exception e) {
      e.printStackTrace();
    }


    if(len==8) {

      String data_str = "";

      int i=0;

      System.out.println("\r\nnew trace\r\n");

      while(true) {
        len = nanoVNA_port.readBytes( data_buffer, 64000, 0);
        if(len>0) {
          data_buffer[len]=0;
          data_str = data_str.concat( new String(data_buffer) );
          i=0;
        }
        if( new String(data_buffer).contains("\r\nch>") ) break;


        try {
          Thread.sleep(1);
        } catch(Exception e) {
          e.printStackTrace();
        }
        if(i++>5000) break;
      }

      if(data_str.length()>0) {
        //System.out.println("-----------------------------------------");
        //System.out.print( new String(data_str) );
        //System.out.println("-----------------------------------------");
      }

      StringTokenizer st = new StringTokenizer(data_str, "\r\n");
      int line=0;
      int data_lines=0;

      double real[] = new double[101];
      double imag[] = new double[101];

      while( st.hasMoreTokens() ) {
        String str1 = new String( st.nextToken() );
        System.out.println(line +" "+str1);

        if(line>=1 && line<=101) {
          String tokr="";
          String toki="";
          StringTokenizer st2 = new StringTokenizer(str1," ");
          if(st2.countTokens()==2) {
            real[line-1] = new Double( st2.nextToken() ).doubleValue();
            imag[line-1] = new Double( st2.nextToken() ).doubleValue();
            //if(real[line-1] > 1.0) real[line-1]=1.0;
            //if(real[line-1] < -1.0) real[line-1]=-1.0;
            //if(imag[line-1] > 1.0) imag[line-1]=1.0;
            //if(imag[line-1] < -1.0) imag[line-1]=-1.0;
            data_lines++;
          }
        }

        line++;
      }

      System.out.println("data lines: "+data_lines);

      double freq = 50e3;
      double freq_step = ((900e6-50e3)/100.0);
      i=0;
      double[] freqs = new double[101];
      double[] mag = new double[101];

      if(data_type==0) {
        real_s11 = new double[101];
        imag_s11 = new double[101];

        for(i=0; i<101; i++) {
          real_s11[i] = real[i];
          imag_s11[i] = imag[i];
        }
      }
      if(data_type==1) {
        real_s21 = new double[101];
        imag_s21 = new double[101];

        for(i=0; i<101; i++) {
          real_s21[i] = real[i];
          imag_s21[i] = imag[i];
        }
      }

      for(i=0; i<101; i++) {
        freqs[i] = 50e3 + freq_step*i;
        mag[i] = 20.0 * java.lang.Math.log10( java.lang.Math.pow( real[i]*real[i] + imag[i]*imag[i], 0.5) );
      }

      //appears everything got collected ok
      if(data_lines==101) {

        if(data_type==0) smith_panel.plotS11(freqs, real, imag);
        if(data_type==0) mag_plot.plotS11(freqs, mag);
        if(data_type==1) mag_plot.plotS21(freqs, mag);

        if(data_type==0 && do_write_s1p.isSelected() && real_s11!=null) touchstone.write1p(freqs, real_s11, imag_s11); //write touchstone s1p to current dir 
        if(data_type==1 && do_write_s2p.isSelected() && real_s11!=null && real_s21!=null) touchstone.write2p(freqs, real_s11, imag_s11, real_s21, imag_s21); //write touchstone s1p to current dir 

        plot_count++;
        count_label.setText(" plot iteration: "+plot_count);
      }

    }
  }

  //////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////
  public VNAFrame()
  {

    initComponents();

    count_label = new javax.swing.JLabel();
    jPanel4.add(count_label);

    jTabbedPane1.remove(jPanel2);

    smith_panel = new smithChartPanel();
    mag_plot = new magPanel();
    touchstone = new writeTouchStone();

    jTabbedPane1.addTab("Smith Chart", smith_panel);
    jTabbedPane1.addTab("Mag Plot", mag_plot);


    //find the serial port or exit
    SerialPort[] ports = SerialPort.getCommPorts();
    for(int i=0; i<ports.length; i++) {
      System.out.println("\r\nport: "+ports[i]);
      System.out.println("\r\nport device: "+ports[i].getSystemPortName());

      if( ports[i].toString().startsWith("ChibiOS/RT Virtual COM Port") ) { //we are looking for this string in the serial port description
        nanoVNA_port = ports[i];
        System.out.println("\r\nfound nanoVNA serial port on device: "+ports[i].getSystemPortName());
        jLabel2.setText("Using serial port named: "+ports[i].getSystemPortName());
      }
    }

    if( nanoVNA_port==null ) {
      System.out.println("\r\ncould not find the nanoVNA device serial port. exiting.");
      System.exit(0);
    }

    int retry=0;
    for(retry=0;retry<20;retry++) {
      if( nanoVNA_port.openPort(1000)==false) {
        System.out.println("\r\ncould not open the nanoVNA device serial port. exiting. retry...");
      } else {
        nanoVNA_port.setBaudRate( 2000000 ); //this probably doesn't really matter
        nanoVNA_port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);
        break;
      }
    }

    if(retry==20) {
      System.out.println("\r\ncould not open the nanoVNA device serial port after 20 retries. exiting.");
      System.exit(0);
    }



    utimer = new java.util.Timer();
    utimer.schedule( new updateTask(), 1000, 60);
  }


  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        sweep_toggle = new javax.swing.JToggleButton();
        jLabel2 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        do_plot_s11 = new javax.swing.JCheckBox();
        do_plot_s21 = new javax.swing.JCheckBox();
        do_write_s1p = new javax.swing.JCheckBox();
        do_write_s2p = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel1.setText("jNanoVNA,  alpha 0.1, 2019");
        jPanel3.add(jLabel1);

        getContentPane().add(jPanel3, java.awt.BorderLayout.NORTH);

        jTabbedPane1.setPreferredSize(new java.awt.Dimension(800, 600));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1011, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 686, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("tab1", jPanel2);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        sweep_toggle.setText("Sweep");
        sweep_toggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sweep_toggleActionPerformed(evt);
            }
        });
        jPanel4.add(sweep_toggle);

        jLabel2.setText("Using serial port named: ");
        jPanel4.add(jLabel2);

        jPanel1.add(jPanel4);

        jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        do_plot_s11.setSelected(true);
        do_plot_s11.setText("Plot S11");
        jPanel5.add(do_plot_s11);

        do_plot_s21.setText("Plot S21");
        jPanel5.add(do_plot_s21);

        do_write_s1p.setText("Write s1p touchstone");
        jPanel5.add(do_write_s1p);

        do_write_s2p.setText("Write s2p touchstone");
        jPanel5.add(do_write_s2p);

        jPanel1.add(jPanel5);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void sweep_toggleActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_sweep_toggleActionPerformed
  {
  }//GEN-LAST:event_sweep_toggleActionPerformed

  public static void main(String args[])
  {
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
    /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
     * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
     */
    try {
      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          javax.swing.UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (ClassNotFoundException ex) {
      java.util.logging.Logger.getLogger(VNAFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
      java.util.logging.Logger.getLogger(VNAFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      java.util.logging.Logger.getLogger(VNAFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
      java.util.logging.Logger.getLogger(VNAFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        new VNAFrame().setVisible(true);
      }
    });
  }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox do_plot_s11;
    private javax.swing.JCheckBox do_plot_s21;
    private javax.swing.JCheckBox do_write_s1p;
    private javax.swing.JCheckBox do_write_s2p;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToggleButton sweep_toggle;
    // End of variables declaration//GEN-END:variables
}
