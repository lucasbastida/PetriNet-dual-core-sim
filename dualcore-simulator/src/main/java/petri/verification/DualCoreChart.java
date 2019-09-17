package verification;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;




public class DualCoreChart extends ApplicationFrame {

    private DualCoreChart(final String title) {
        super(title);
        final JFreeChart chart = createCombinedChart();
        final ChartPanel panel = new ChartPanel(chart, true, true, true, false, true);
        panel.setPreferredSize(new java.awt.Dimension(1000, 600));
        setContentPane(panel);
    }

    private JFreeChart createCombinedChart() {

        // create subplot 1...
        final XYDataset data1 = createDataset1();
        final XYItemRenderer renderer1 = new StandardXYItemRenderer();
        final NumberAxis rangeAxis1 = new NumberAxis("Amount");
        final XYPlot subplot1 = new XYPlot(data1, null, rangeAxis1, renderer1);
        subplot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

        // parent plot...
        final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis("ms"));
        plot.setGap(10.0);

        // add the subplots...
        plot.add(subplot1, 1);
        plot.setOrientation(PlotOrientation.VERTICAL);

        //modify colors and line stroke size
        subplot1.setBackgroundPaint(Color.GRAY);
        subplot1.setDomainGridlinePaint(Color.BLACK);
        subplot1.setRangeGridlinePaint(Color.BLACK);

        XYItemRenderer xyir = subplot1.getRenderer();
        BasicStroke stroke = new BasicStroke(3);

        for (int series = 0; series < subplot1.getDataset().getSeriesCount(); series++) {
            xyir.setSeriesStroke(series, stroke);

        }

        // return a new chart containing the overlaid plot...
        return new JFreeChart("Tareas en Buffer & total procesado",
                JFreeChart.DEFAULT_TITLE_FONT, plot, true);

    }

    private XYDataset createDataset1() {
        final XYSeries seriesBuffer1 = new XYSeries("Tareas en Buffer 1");
        final XYSeries seriesBuffer2 = new XYSeries("Tareas en Buffer 2");
        final XYSeries seriesTotal1 = new XYSeries("Total procesado en 1");
        final XYSeries seriesTotal2 = new XYSeries("Total procesado en 2");

        Pattern time = Pattern.compile("runtime since inicio: (\\d+)ms");
        Pattern buffer1 = Pattern.compile("buffer1=(\\d+)");
        Pattern buffer2 = Pattern.compile("buffer2=(\\d+)");
        Pattern totalProcesadas1 = Pattern.compile("totalProcesadas1 =(\\d+)");
        Pattern totalProcesadas2 = Pattern.compile("totalProcesadas2 =(\\d+)");

        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader("log.txt"));

            String line = reader.readLine();

            int ms = 0;
            int b1 = 0;
            int b2 = 0;
            int total1 = 0;
            int total2;

            while (line != null) {
                Matcher timeMatcher = time.matcher(line);
                Matcher buffer1Matcher = buffer1.matcher(line);
                Matcher buffer2Matcher = buffer2.matcher(line);
                Matcher total1Matcher = totalProcesadas1.matcher(line);
                Matcher total2Matcher = totalProcesadas2.matcher(line);

                if (timeMatcher.matches()) {
                    ms = Integer.parseInt(timeMatcher.group(1));
                } else if (buffer1Matcher.matches()) {
                    b1 = Integer.parseInt(buffer1Matcher.group(1));
                } else if (buffer2Matcher.matches()) {
                    b2 = Integer.parseInt(buffer2Matcher.group(1));
                } else if (total1Matcher.matches()) {
                    total1 = Integer.parseInt(total1Matcher.group(1));
                } else if (total2Matcher.matches()) {
                    total2 = Integer.parseInt(total2Matcher.group(1));

                    System.out.println(ms);
                    seriesBuffer1.add(ms, b1);
                    seriesBuffer2.add(ms, b2);
                    seriesTotal1.add(ms, total1);
                    seriesTotal2.add(ms, total2);
                }

                line = reader.readLine(); //read next line
            }

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        final XYSeriesCollection collection = new XYSeriesCollection();
        collection.addSeries(seriesBuffer1);
        collection.addSeries(seriesBuffer2);
        collection.addSeries(seriesTotal1);
        collection.addSeries(seriesTotal2);
        return collection;

    }

    public static void main(final String[] args) {
        final DualCoreChart demo = new DualCoreChart("Dual core results");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }
}