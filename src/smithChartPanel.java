
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
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import java.lang.Math.*;

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
public class smithChartPanel extends JPanel implements Runnable
{

  int w;
  int h;
  int hw;
  int hh;
  int cx;
  int cy;
  double[] data_real;
  double[] data_imag;
  double freq_mhz[];
  boolean draw_s11=false;
  double scale_factor = 0.85;
  Color bg;
  Color fg;
  Color line_color;
  Color vswr_color;
  boolean do_antialias=false;

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
  public smithChartPanel()
  {
    line_color = new Color(220,220,220);
    fg = Color.black;
    bg = new Color(200,200,200);
    vswr_color = Color.blue;
    do_antialias=true;
    setPreferredSize( new Dimension(1024,768) );
  }

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
  public void plotS11(double freq[], double[] re, double[] im)
  {
    if(freq==null || freq.length==0) return;
    freq_mhz = freq;
    data_real = re;
    data_imag = im;
    draw_s11=true;
    repaint();
  }

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
  public void paint(Graphics g)
  {
    Graphics2D g2d = (Graphics2D) g;
    Font font = new Font("Arial", Font.PLAIN, 18);
    g2d.setFont(font);

    if(do_antialias) {
      g2d.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2d.setRenderingHint(
        RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }


    //w = getWidth();
    h = getHeight();
    //h = (int) (0.75*(double)w);
    w = (int)((double)h*1.33);

    cx = w/2;
    cy = h/2;
    hw = w/2;
    hh = h/2;

    //clear background
    g2d.setColor( bg );
    g2d.fillRect( 0,0,getWidth(),getHeight());

    //outer circle
    draw_outer_circle(g2d);


    //draw circles of real Z
    draw_realz(g2d, 0.05);
    draw_realz(g2d, 0.2);
    draw_realz(g2d, 0.5);
    draw_realz(g2d, 1.0);
    draw_realz(g2d, 2.0);

    draw_reactive(g2d,2.5);
    draw_reactive(g2d,10.0);
    draw_reactive(g2d,25.0);
    draw_reactive(g2d,50.0);
    draw_reactive(g2d,100.0);
    draw_reactive(g2d,-2.5);
    draw_reactive(g2d,-10.0);
    draw_reactive(g2d,-25.0);
    draw_reactive(g2d,-50.0);
    draw_reactive(g2d,-100.0);

    //resistance line
    BasicStroke bs = new BasicStroke(1.0f);
    g2d.setStroke(bs);
    g2d.setPaint(line_color);
    Line2D.Double l2d = new Line2D.Double( (double) w*(0.15/2.0), (double) cy, (double) w*(1.0-(.15/2.0)), (double) cy);
    g2d.draw(l2d);

    //circles of RHO
    draw_rho(g2d);


    g2d.setColor( Color.white );
    bs = new BasicStroke(40.0f);
    g2d.setStroke(bs);
    Rectangle2D.Double bgr = new Rectangle2D.Double(20,20,w-40,h-40);
    g2d.draw(bgr);
    bs = new BasicStroke(2.0f);
    g2d.setStroke(bs);
    g2d.setColor( Color.black );
    bgr = new Rectangle2D.Double(40,40,w-80,h-80);
    g2d.draw(bgr);



    //TODO: allow user to add a VSWR/RL label for a given frequency
    g2d.setPaint(vswr_color);
    g2d.drawString("VSWR Circle = 2.0, RL=9.5dB  _____", 50,125);

    if(draw_s11) {
      int len = freq_mhz.length;
      double px=0.0;
      double py=0.0;

      bs = new BasicStroke(1.0f);
      g2d.setStroke(bs);

      g2d.setPaint(fg);
      g2d.drawString(String.format("Frequency Range: %3.1f kHz to %3.1f MHz",freq_mhz[0]/1e3,freq_mhz[len-1]/1e6), 50,75);

      g2d.setPaint(Color.yellow);
      g2d.drawString("S11 _____", 50,100);

      for(int i=0; i<len; i++) {
        double real = data_real[i];
        double imag = data_imag[i];
        double x = real * hw * scale_factor;
        double y = imag * hh * scale_factor;


        y *= -1.0;
        x+=cx;
        y+=cy;

        if(px>0) {
          l2d = new Line2D.Double( (double) x, (double) y, (double) px, (double) py);
          g2d.draw(l2d);
        }
        px = x;
        py = y;
      }
    }

    //imaginary component labels
    draw_imag_labels(g2d, 0.0, 0.0, "0 + j0", Color.black);
    draw_imag_labels(g2d, 2.5, 0.0, "2.5+j0", Color.black);
    draw_imag_labels(g2d, 10.0, 0.0, "10+j0", Color.black);
    draw_imag_labels(g2d, 25.0, 0.0, "25+j0", Color.black);
    draw_imag_labels(g2d, 50.0, 0.0, "50+j0", Color.black);
    draw_imag_labels(g2d, 100.0, 0.0, "100+j0", Color.black);

    draw_imag_labels(g2d, 75.0, 0.0, "75+j0", Color.black);
    draw_imag_labels(g2d, 300.0, 0.0, "300+j0", Color.black);

    //draw_imag_labels(g2d, 10.0e6, 0.0, "inf", Color.black);
    //attemp drawng infinity sign
    draw_imag_labels_off(g2d, 100.0e9, 0.0, "O", Color.black,0,0);
    draw_imag_labels_off(g2d, 100.0e9, 0.0, "O", Color.black,4,0);

    draw_imag_labels_off(g2d, 14.0, 10.0, " /\\ Inductive j>0", Color.white,6,0);
    draw_imag_labels_off(g2d, 14.0, 0.0, "Pure Resistive j=0", Color.white,6,0);
    draw_imag_labels_off(g2d, 14.0, -10.0, "\\/ Capactive j<0", Color.white,6,0);

    draw_imag_labels_off(g2d, 376.73, 0.0, "*", Color.red,6,3);
    draw_imag_labels_off(g2d, 400.0, 0.0, "<-Free-Space Z", Color.black,6,3);

    draw_imag_labels(g2d, 0.0, 2.5, "0.0 + j2.5", Color.black);
    draw_imag_labels(g2d, 0.0, 10.0, "0.0 + j10.0", Color.black);
    draw_imag_labels(g2d, 0.0, 25.0, "0.0 + j25.0", Color.black);
    draw_imag_labels(g2d, 0.0, 50.0, "0.0 + j50.0", Color.black);
    draw_imag_labels(g2d, 0.0, 100.0, "0.0 + j100.0", Color.black);
    draw_imag_labels(g2d, 0.0, -2.5, "0.0 - j2.5", Color.black);
    draw_imag_labels(g2d, 0.0, -10.0, "0.0 - j10.0", Color.black);
    draw_imag_labels(g2d, 0.0, -25.0, "0.0 - j25.0", Color.black);
    draw_imag_labels(g2d, 0.0, -50.0, "0.0 - j50.0", Color.black);
    draw_imag_labels(g2d, 0.0, -100.0, "0.0 - j100.0", Color.black);

    //draw_imag_labels(g2d, 2.5, 2.5, "2.5 + j2.5", Color.black);
    draw_imag_labels(g2d, 2.5, 10.0, "2.5 + j10.0", Color.black);
    draw_imag_labels(g2d, 2.5, 25.0, "2.5 + j25.0", Color.black);
    draw_imag_labels(g2d, 2.5, 50.0, "2.5 + j50.0", Color.black);
    //draw_imag_labels(g2d, 2.5, -2.5, "2.5 - j2.5", Color.black);
    draw_imag_labels(g2d, 2.5, -10.0, "2.5 - j10.0", Color.black);
    draw_imag_labels(g2d, 2.5, -25.0, "2.5 - j25.0", Color.black);
    draw_imag_labels(g2d, 2.5, -50.0, "2.5 - j50.0", Color.black);

    draw_imag_labels(g2d, 10.0, 2.5, "10 + j2.5", Color.black);
    draw_imag_labels(g2d, 10.0, 10.0, "10 + j10.0", Color.black);
    draw_imag_labels(g2d, 10.0, 25.0, "10 + j25.0", Color.black);
    draw_imag_labels(g2d, 10.0, 50.0, "10 + j50.0", Color.black);
    draw_imag_labels(g2d, 10.0, 100.0, "10 + j100.0", Color.black);
    draw_imag_labels(g2d, 10.0, -2.5, "10 - j2.5", Color.black);
    draw_imag_labels(g2d, 10.0, -10.0, "10 - j10.0", Color.black);
    draw_imag_labels(g2d, 10.0, -25.0, "10 - j25.0", Color.black);
    draw_imag_labels(g2d, 10.0, -50.0, "10 - j50.0", Color.black);
    draw_imag_labels(g2d, 10.0, -100.0, "10 - j100.0", Color.black);


    draw_imag_labels(g2d, 25.0, 25.0, "25 + j25.0", Color.black);
    draw_imag_labels(g2d, 25.0, 50.0, "25 + j50.0", Color.black);
    draw_imag_labels(g2d, 25.0, 100.0, "25 + j100.0", Color.black);
    draw_imag_labels(g2d, 25.0, -25.0, "25 - j25.0", Color.black);
    draw_imag_labels(g2d, 25.0, -50.0, "25 - j50.0", Color.black);
    draw_imag_labels(g2d, 25.0, -100.0, "25 - j100.0", Color.black);

    draw_imag_labels(g2d, 50.0, 50.0, "50.0 + j50.0", Color.black);
    draw_imag_labels(g2d, 50.0, 100.0, "50.0 + j100.0", Color.black);
    draw_imag_labels(g2d, 50.0, -50.0, "50.0 - j50.0", Color.black);
    draw_imag_labels(g2d, 50.0, -100.0, "50.0 - j100.0", Color.black);

    draw_imag_labels(g2d, 100.0, 50.0, "100.0 + j50.0", Color.black);
    draw_imag_labels(g2d, 100.0, 100.0, "100.0 + j100.0", Color.black);
    draw_imag_labels(g2d, 100.0, -50.0, "100.0 - j50.0", Color.black);
    draw_imag_labels(g2d, 100.0, -100.0, "100.0 - j100.0", Color.black);

  }
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
  private void draw_imag_labels(Graphics2D g2d, double real, double img, String label, Color col)
  {
    BasicStroke bs = new BasicStroke(1.0f);
    g2d.setStroke(bs);
    g2d.setPaint(col);

    Complex ref = new Complex(50.0,0.0);
    Complex c = new Complex(real,img);
    Complex z1 = c.minus(ref);
    Complex z2 = c.plus(ref);
    Complex z = z1.div(z2);

    double x = z.real() * hw * scale_factor;
    double y = z.imag() * hh * scale_factor;
    y *= -1.0;
    x+=cx;
    y+=cy;

    Font font = new Font("Arial", Font.PLAIN, 8);
    g2d.setFont(font);
    g2d.drawString(label,(int) x,(int) y);
    font = new Font("Arial", Font.PLAIN, 18);
    g2d.setFont(font);
  }
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
  private void draw_imag_labels_off(Graphics2D g2d, double real, double img, String label, Color col, int off_x, int off_y)
  {
    BasicStroke bs = new BasicStroke(1.0f);
    g2d.setStroke(bs);
    g2d.setPaint(col);

    Complex ref = new Complex(50.0,0.0);
    Complex c = new Complex(real,img);
    Complex z1 = c.minus(ref);
    Complex z2 = c.plus(ref);
    Complex z = z1.div(z2);

    double x = z.real() * hw * scale_factor;
    double y = z.imag() * hh * scale_factor;
    y *= -1.0;
    x+=cx+off_x;
    y+=cy+off_y;

    Font font = new Font("Arial", Font.PLAIN, 8);
    g2d.setFont(font);
    g2d.drawString(label,(int) x,(int) y);
    font = new Font("Arial", Font.PLAIN, 18);
    g2d.setFont(font);
  }

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
  private void draw_reactive(Graphics2D g2d, double img)
  {
    BasicStroke bs = new BasicStroke(1.0f);
    g2d.setStroke(bs);
    g2d.setPaint(line_color);
    double px = 0.0;
    double py = 0.0;

    double real = 0.0;
    while(real<10e3) {
      Complex ref = new Complex(50.0,0.0);
      Complex c = new Complex(real,img);
      Complex z1 = c.minus(ref);
      Complex z2 = c.plus(ref);
      Complex z = z1.div(z2);

      double x = z.real() * hw * scale_factor;
      double y = z.imag() * hh * scale_factor;
      y *= -1.0;
      x+=cx;
      y+=cy;

      if(px>0) {
        Line2D.Double l2d = new Line2D.Double(px,py,x,y);
        g2d.draw(l2d);
      }
      px = x;
      py = y;

      if(real<1000.0) real+=1.0;
      else real+=100.0;
    }

  }

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
  private void draw_realz(Graphics2D g2d, double v)
  {
    double a = 0.0;
    double px=0.0;
    double py=0.0;

    BasicStroke bs = new BasicStroke(1.0f);
    g2d.setStroke(bs);
    g2d.setPaint(line_color);

    while(a<2*Math.PI) {
      double v1 = 1.0/(v+1.0);
      double v2 = v/(v+1.0);

      double x = v2*hw*scale_factor + (v1 * Math.cos(a)*(double)hw*scale_factor);
      double y = v1 * Math.sin(a)*(double)hh*scale_factor;
      x += cx;
      y += cy;
      /*
            if( Math.abs(cy-y)<25.0) {
              bs = new BasicStroke(3.0f);
              g2d.setStroke(bs);
              g2d.setPaint(Color.white);
            }
            else {
              bs = new BasicStroke(1.0f);
              g2d.setStroke(bs);
              g2d.setPaint(line_color);
            }
      */
      if(a>0) {
        Line2D.Double l2d = new Line2D.Double(px,py,x,y);
        g2d.draw(l2d);
      }
      px = x;
      py = y;
      a+=Math.PI/100.0;
    }

  }

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
  private void draw_outer_circle(Graphics2D g2d)
  {
    double a = 0.0;
    double px=0.0;
    double py=0.0;

    BasicStroke bs = new BasicStroke(1.0f);
    g2d.setStroke(bs);
    g2d.setPaint(line_color);


    while(a<2*Math.PI) {
      double x = Math.cos(a)*(double)hw*scale_factor;
      double y = Math.sin(a)*(double)hh*scale_factor;
      x += cx;
      y += cy;

      /*
            if( Math.abs(cy-y)<25.0) {
              bs = new BasicStroke(3.0f);
              g2d.setStroke(bs);
              g2d.setPaint(Color.white);
            }
            else {
              bs = new BasicStroke(1.0f);
              g2d.setStroke(bs);
              g2d.setPaint(line_color);
            }
      */
      if(a>0) {
        Line2D.Double l2d = new Line2D.Double(px,py,x,y);
        g2d.draw(l2d);
      }
      px = x;
      py = y;
      a+=Math.PI/100.0;
    }
  }

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
  private void draw_rho(Graphics2D g2d)
  {
    double a = 0.0;
    double px=0.0;
    double py=0.0;

    BasicStroke bs = new BasicStroke(2.0f);
    g2d.setStroke(bs);
    g2d.setPaint(vswr_color);


    while(a<2*Math.PI) {
      double x = Math.cos(a)*(double)hw*scale_factor*0.32;
      double y = Math.sin(a)*(double)hh*scale_factor*0.32;
      x += cx;
      y += cy;

      if(a>0) {
        Line2D.Double l2d = new Line2D.Double(px,py,x,y);
        g2d.draw(l2d);
      }
      px = x;
      py = y;
      a+=Math.PI/100.0;
    }
  }

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
  public static void main(String[] args)
  {
    smithChartPanel scp = new smithChartPanel();
    SwingUtilities.invokeLater(scp);
  }

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
  public void run()
  {
    JFrame frame = new JFrame("");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setPreferredSize(new Dimension(800, 800));
    frame.pack();
    frame.getContentPane().add(this);
    frame.setVisible(true);
  }
}
