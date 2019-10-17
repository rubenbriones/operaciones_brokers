import java.awt.BorderLayout;   
import java.awt.Color;   
import java.awt.Dimension;   
import java.text.SimpleDateFormat;   
import java.util.Date;   
   
import javax.swing.BorderFactory;   
import javax.swing.JPanel;   
import javax.swing.JFrame;   
import javax.swing.JScrollPane;   
import javax.swing.JSlider;   
import javax.swing.JTable;   
import javax.swing.border.Border;   
import javax.swing.event.ChangeEvent;   
import javax.swing.event.ChangeListener;   
import javax.swing.table.AbstractTableModel;   
import javax.swing.table.TableCellRenderer;   
import javax.swing.table.TableModel;   
   
import org.jfree.chart.ChartFactory;   
import org.jfree.chart.ChartPanel;   
import org.jfree.chart.JFreeChart;   
import org.jfree.chart.axis.ValueAxis;   
import org.jfree.chart.event.ChartProgressEvent;   
import org.jfree.chart.event.ChartProgressListener;   
import org.jfree.chart.plot.PlotOrientation;   
import org.jfree.chart.plot.XYPlot;   
import org.jfree.chart.renderer.xy.XYItemRenderer;   
import org.jfree.data.Range;   
import org.jfree.data.time.Day;   
import org.jfree.data.time.RegularTimePeriod;   
import org.jfree.data.time.TimeSeries;   
import org.jfree.data.time.TimeSeriesCollection;   
import org.jfree.data.time.TimeSeriesDataItem;   
import org.jfree.data.xy.XYDataset;   
import org.jfree.ui.ApplicationFrame;   
import org.jfree.ui.DateCellRenderer;   
import org.jfree.ui.NumberCellRenderer;   
import org.jfree.ui.RectangleInsets;   
import org.jfree.ui.RefineryUtilities;   
   
/**  
 * An example of a crosshair being controlled by an external UI component.  
 */   
public class CrosshairDemo1 extends JFrame {   
   
    private static class DemoPanel extends JPanel implements ChangeListener, ChartProgressListener{
        private static final int SERIES_COUNT = 2;        
        private TimeSeries[] series;   
        private int numItems;
        
        private ChartPanel chartPanel;   
           
        private DemoTableModel model;   
           
        private JFreeChart chart;   
           
        private JSlider slider;   
   
        /**  
         * Creates a new demo panel.  
         */   
        public DemoPanel(JFreeChart jfreeChart, TimeSeries seriesAcc, TimeSeries seriesBrok) {   
            super(new BorderLayout());   
            
            this.series = new TimeSeries[SERIES_COUNT];
            this.series[0] = seriesAcc;
            this.series[1] = seriesBrok;
            numItems = seriesAcc.getItemCount(); //He hecho una mod. en GraficoBrokerAccionV2 para que los dos timeseries sean igual de longitud.
            
            //this.chart = createChart();   
            this.chart = jfreeChart;
            this.chart.addProgressListener(this);   
            this.chartPanel = new ChartPanel(this.chart);   
            this.chartPanel.setPreferredSize(new java.awt.Dimension(600, 270));   
            this.chartPanel.setDomainZoomable(true);   
            this.chartPanel.setRangeZoomable(true);   
            Border border = BorderFactory.createCompoundBorder(   
                BorderFactory.createEmptyBorder(4, 4, 4, 4),   
                BorderFactory.createEtchedBorder()   
            );   
            this.chartPanel.setBorder(border);   
            add(this.chartPanel);   
               
            JPanel dashboard = new JPanel(new BorderLayout());   
            dashboard.setPreferredSize(new Dimension(400, 75));   
            dashboard.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));   
               
            this.model = new DemoTableModel(SERIES_COUNT);
            for(int i=0; i < SERIES_COUNT; i++) {
                this.model.setValueAt(this.chart.getXYPlot().getDataset(i).getSeriesKey(0), i, 0);   
                /*this.model.setValueAt(new Double("0.00"), i, 1);   
                this.model.setValueAt(new Double("0.00"), i, 2);   
                this.model.setValueAt(new Double("0.00"), i, 3);   
                this.model.setValueAt(new Double("0.00"), i, 4);   
                this.model.setValueAt(new Double("0.00"), i, 5);   
                this.model.setValueAt(new Double("0.00"), i, 6);*/
            }
            
            JTable table = new JTable(this.model);   
            TableCellRenderer renderer1 = new DateCellRenderer(   
                new SimpleDateFormat("dd/MM/yyyy")   
            );   
            TableCellRenderer renderer2 = new NumberCellRenderer();   
            table.getColumnModel().getColumn(1).setCellRenderer(renderer1);   
            table.getColumnModel().getColumn(2).setCellRenderer(renderer2);   
            table.getColumnModel().getColumn(3).setCellRenderer(renderer1);   
            table.getColumnModel().getColumn(4).setCellRenderer(renderer2);   
            table.getColumnModel().getColumn(5).setCellRenderer(renderer1);   
            table.getColumnModel().getColumn(6).setCellRenderer(renderer2);   
            table.getColumnModel().getColumn(7).setCellRenderer(renderer2);  
            table.getColumnModel().getColumn(8).setCellRenderer(renderer2);  
            table.getColumnModel().getColumn(9).setCellRenderer(renderer2);  
            dashboard.add(new JScrollPane(table));   
               
            this.slider = new JSlider(0, numItems-1, 0);   
            this.slider.addChangeListener(this);   
            dashboard.add(this.slider, BorderLayout.SOUTH);   
            add(dashboard, BorderLayout.SOUTH); 
        }   
           
        /**  
         * Handles a state change event.  
         *   
         * @param event  the event.  
         */   
        public void stateChanged(ChangeEvent event) {   
            int value = this.slider.getValue();   
            XYPlot plot = this.chart.getXYPlot();
            
            //ValueAxis domainAxis = plot.getDomainAxis();   
            //Range range = domainAxis.getRange();   
            //double c = domainAxis.getLowerBound() + (value / 100.0) * range.getLength(); 
            
            /*
            double valorFechaIni = series[0].getTimePeriod(0).getFirstMillisecond();
            double valorFechaFin = series[0].getTimePeriod(numItems-1).getFirstMillisecond();
            double rango = valorFechaFin - valorFechaIni;
            double c = valorFechaIni + (value / (double)numItems) * rango;            
            plot.setDomainCrosshairValue(c);
            */
           
            plot.setDomainCrosshairValue(series[0].getTimePeriod(value).getFirstMillisecond());
        }   
   
        /**  
         * Handles a chart progress event.  
         *   
         * @param event  the event.  
         */   
        public void chartProgress(ChartProgressEvent event) {   
            if (event.getType() != ChartProgressEvent.DRAWING_FINISHED) {   
                return;   
            }   
            if (this.chartPanel != null) {   
                JFreeChart c = this.chartPanel.getChart();   
                if (c != null) {   
                    XYPlot plot = c.getXYPlot();   
                    XYDataset dataset = plot.getDataset();   
                    Comparable seriesKey = dataset.getSeriesKey(0);   
                    double xx = plot.getDomainCrosshairValue();  //valor en millis de la fecha seleccionada con el crosshair
   
                    // update the table...   
                    //this.model.setValueAt(seriesKey, 0, 0);   
                    long millis = (long) xx; //valor en millis de la fecha seleccionada con el crosshair
                    //this.model.setValueAt(new Long(millis), 0, 1);
                    
                    double close=0, closePrev=0, closeNext=0;
                    for(int i=0; i < SERIES_COUNT; i++) {
                        int itemIndex = this.series[i].getIndex( new Day(new Date(millis)) );   
                        if (itemIndex >= 0) {   
                            TimeSeriesDataItem item = this.series[i].getDataItem( Math.min(numItems-1, Math.max(0, itemIndex)) );   
                            TimeSeriesDataItem prevItem = this.series[i].getDataItem( Math.max(0, itemIndex - 1) );   
                            TimeSeriesDataItem nextItem = this.series[i].getDataItem( Math.min(numItems-1, itemIndex + 1) );
                            
                            long x = item.getPeriod().getMiddleMillisecond();         
                            double y = item.getValue().doubleValue();
                            long prevX = prevItem.getPeriod().getMiddleMillisecond();         
                            double prevY = prevItem.getValue().doubleValue(); 
                            long nextX = nextItem.getPeriod().getMiddleMillisecond();   
                            double nextY = nextItem.getValue().doubleValue();
                            
                            this.model.setValueAt(new Long(x), i, 1);   
                            this.model.setValueAt(new Double(y), i, 2);   
                            this.model.setValueAt(new Long(prevX), i, 3);   
                            this.model.setValueAt(new Double(prevY), i, 4);   
                            this.model.setValueAt(new Long(nextX), i, 5);   
                            this.model.setValueAt(new Double(nextY), i, 6);
                            
                            //Ahora relleno las columnas que solo hay que calcular en la fila del broker.
                            if(i==1){
                                this.model.setValueAt(new Integer((int)(y*close)), i, 7);
                                this.model.setValueAt(new Integer((int)(prevY*closePrev)), i, 8);
                                this.model.setValueAt(new Integer((int)(nextY*closeNext)), i, 9);
                            }
                            
                            //Me guardo los valores de cierre de la accion, para luego multipicarlos por los titulos en la segunda iteracion.
                            //Al final de la segunda iteracion se guardaran el numero de titulos, pero ese dato ya no me hace falta y no lo uso.
                            close=y;              
                            closePrev=prevY;  
                            closeNext=nextY;
                        }   
                    }
                }   
            }   
        }   
   
    }   
       
    /**  
     * A demonstration application showing how to control a crosshair using an  
     * external UI component.  
     *  
     * @param title  the frame title.  
     */   
    public CrosshairDemo1(String title, JFreeChart chart, TimeSeries seriesAcc, TimeSeries seriesBrok) {   
        super(title); 
        setContentPane(new DemoPanel(chart, seriesAcc, seriesBrok));
    }   
   
    /**  
     * Creates a panel for the demo (used by SuperDemo.java).  
     *   
     * @return A panel.  
     */   
    public static JPanel createDemoPanel(JFreeChart chart, TimeSeries seriesAcc, TimeSeries seriesBrok) {   
        return new DemoPanel(chart, seriesAcc, seriesBrok);   
    }   
   
    /**  
     * Starting point for the demonstration application.  
     *  
     * @param args  ignored.  
     */   
    public static void main(String[] args) {   
        /*
        CrosshairDemo1 demo = new CrosshairDemo1("Crosshair Demo 1");   
        demo.pack();   
        RefineryUtilities.centerFrameOnScreen(demo);   
        demo.setVisible(true);   
        */
    }   
   
    /**  
     * A demo table model.  
     */   
    static class DemoTableModel extends AbstractTableModel implements TableModel {   
       
        private Object[][] data;   
       
        /**  
         * Creates a new demo table model.   
         *   
         * @param rows  the row count.  
         */   
        public DemoTableModel(int rows) {   
            this.data = new Object[rows][10];   
        }   
        
        /**  
         * Returns the number of columns.  
         *   
         * @return 7.  
         */   
        public int getColumnCount() {   
            return 10;   
        }   
           
        /**  
         * Returns the row count.  
         *   
         * @return 1.  
         */   
        public int getRowCount() {   
            return this.data.length;    
        }   
           
        /**  
         * Returns the value at the specified cell in the table.  
         *   
         * @param row  the row index.  
         * @param column  the column index.  
         *   
         * @return The value.  
         */   
        public Object getValueAt(int row, int column) {   
            return this.data[row][column];   
        }   
           
        /**  
         * Sets the value at the specified cell.  
         *   
         * @param value  the value.  
         * @param row  the row index.  
         * @param column  the column index.  
         */   
        public void setValueAt(Object value, int row, int column) {   
            this.data[row][column] = value;   
            fireTableDataChanged();   
        }   
           
        /**  
         * Returns the column name.  
         *   
         * @param column  the column index.  
         *   
         * @return The column name.  
         */   
        public String getColumnName(int column) {   
            switch(column) {   
                case 0 : return "Series Name:";   
                case 1 : return "X:";   
                case 2 : return "Y:";   
                case 3 : return "X (prev)";   
                case 4 : return "Y (prev):";   
                case 5 : return "X (next):";   
                case 6 : return "Y (next):";   
                case 7 : return "Y DINERO:";  
                case 8 : return "Y (PREV) DINERO:";  
                case 9 : return "Y (next) DINERO:";  
            }   
            return null;   
        }   
           
    }   
       
}   