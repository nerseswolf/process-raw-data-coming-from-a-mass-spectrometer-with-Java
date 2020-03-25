import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.Chromatogram;
import uk.ac.ebi.jmzml.model.mzml.ChromatogramList;
import uk.ac.ebi.jmzml.model.mzml.MzML;
import uk.ac.ebi.jmzml.model.mzml.ParamGroup;
import uk.ac.ebi.jmzml.model.mzml.Precursor;
import uk.ac.ebi.jmzml.model.mzml.Product;
import uk.ac.ebi.jmzml.model.mzml.Run;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PeakDetection {
  /*

  Rading data from provided file

   */
  public static final int TIME_ARRAY_INDEX = 0;
  public static final int INTENSITY_ARRAY_INDEX = 1;
  public static void main(final String[] args) {
    final File file = new File("data/P500L-0-550108_1020356857_04_0_1_1_00_1020059252.mzML");
    final List<Chromatogram> chromatograms = new ArrayList<>();
    final MzMLUnmarshaller unmarsh = new MzMLUnmarshaller(file);
    final MzML mzml = unmarsh.unmarshall();
    final Run run = mzml.getRun();
    final ChromatogramList chromatogramList = run.getChromatogramList();
    chromatograms.addAll(chromatogramList.getChromatogram());

    final Iterator<Chromatogram> it = chromatograms.iterator();

    Double[] hatvac = PeakDetection.callDilaog();
    int ccccccccccuka = 0;
    while (it.hasNext()) {

      final Chromatogram chrom = it.next();
      final Precursor precursor = chrom.getPrecursor();
      final Product product = chrom.getProduct();
      if (precursor != null && product != null) {
        final Double mz1 = getMZ(precursor.getIsolationWindow());
        final Double mz2 = getMZ(product.getIsolationWindow());
        final List<BinaryDataArray> binaryDataArray = chrom.getBinaryDataArrayList().getBinaryDataArray();

        // data X values
        final Number[] retentionTimes = binaryDataArray.get(TIME_ARRAY_INDEX).getBinaryDataAsNumberArray();
        //data Y values (not smoothed)
        final Number[] intensities = binaryDataArray.get(INTENSITY_ARRAY_INDEX).getBinaryDataAsNumberArray();

        //data Y values parsed to Double type

        List<Double> intensivity = new ArrayList<>();
        for (int i = 0; i < retentionTimes.length; i++) {
          intensivity.add(intensities[i].doubleValue());
        }

        //data X values parsed to Double type
        Double[] Xer = new Double[retentionTimes.length];
        for (int i = 0; i < retentionTimes.length; i++) {
          Xer[i] = retentionTimes[i].doubleValue();
        }

        //data  Y values (smoothed)
        Double[] doubleLd = SmoothingExp.singleExponentialForecast(intensivity,0.25);

//        if you want to write data to file uncomment the following lines of code


        try {
          new WriteToFile(retentionTimes, doubleLd, ccccccccccuka);
          new WriteToFile(retentionTimes, intensivity, ccccccccccuka);
        }
        catch (Exception e){
          System.out.println(e.getMessage());
        }

          //calculates all the peaks into given range and also calculates periods for that values
        GetPeaks.findPeaks(Xer, doubleLd, hatvac,ccccccccccuka);
        ccccccccccuka++;
        break;
      }
    }
  }
  //0.0018 == 0.0387

  private static Double[] callDilaog() { //Dialog window where user input start and end points of data analize
      JTextField xField = new JTextField(5);
      JTextField yField = new JTextField(5);

      JPanel myPanel = new JPanel();
      myPanel.add(new JLabel("x:"));
      myPanel.add(xField);
      myPanel.add(Box.createHorizontalStrut(15)); // a spacer
      myPanel.add(new JLabel("y:"));
      myPanel.add(yField);
      int result = JOptionPane.showConfirmDialog(null, myPanel,
               "Please Enter X and Y Values", JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION) {
         System.out.println("x value: " + xField.getText());
         System.out.println("y value: " + yField.getText());
      }
      return (new Double[]{Double.valueOf(xField.getText()), Double.valueOf(yField.getText())});
  }

  private static Double getMZ(final ParamGroup param) {
    final List<CVParam> cvParam = param.getCvParam();
    return Double.valueOf(cvParam.get(0).getValue());
  }

}