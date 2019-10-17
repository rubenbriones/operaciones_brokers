/**
 * @author felipe
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 * 
 * Entrada: Número de datos n, datos (x,y)
 */

public class LinearRegression {
    public int mNumPuntos;
    public double mPointX[];
    public double mPointY[];
    public double mPendiente;
    public double mConstante;
    public double mCorrelacion;
    public double mRcuadrado;

    public static void main(){
        double[] mat1 = {2,4,2,3};
        double[] mat2 = {25,25,4,25};
        
        LinearRegression lr = new LinearRegression(mat1, mat2, 4);
        int a=0;
    }
    
    /**
     * Este constructor es si quieres representar puntos que pertenecen a un plano bidimensional (2 ejes, y ele eje X no es correlativo y 
     * fuera de analisis, como por ejemplo 1,2,3,4..). NumeroDeDatos en principio yo diria que es siempre igual a ejeY.lentgh y ejeX.length,
     * pero como no estoy seguro lo dejo como parametro por si acaso (en el archivo original, estaba asi).
     */
    public LinearRegression(double[] ejeX, double[] ejeY, int numeroDeDatos) {
        mPointX = ejeX;
        mPointY = ejeY;
        mNumPuntos = numeroDeDatos;
        calculate();
    }
    
    /**
     * Este constructor es si quieres representar puntos que pertenecen a un plano bidimensional PERO en los que el ejeX es de "mentira",
     * vamos que es correlativo y no representa ninguna variable de analisis, por ejemplo: 1,2,3,4,etc. Así que ya lo construimos nosotros.
     * Asumimos, yo creo que bien hecho, que numeroDeDatos=ejeY.length 
     */
    public LinearRegression(double[] ejeY) {
        double[] ejeX = new double[ejeY.length];
        for(int i=0; i<ejeY.length; i++) ejeX[i] = i+1.0;
        
        mPointX = ejeX;
        mPointY = ejeY;
        mNumPuntos = ejeY.length;
        calculate();
    }

    public void calculate() {
        setPendiente();
        setConstante(); //necesita que se haya ejecutado setPendiente()
        setCorrelacion();
        setRcuadrado(); //necesita que se haya ejecutado setCorrelacion()
    }
    
    /**
     * @return Suma de los datos x
     */
    private double getSumX() {
        double sumX = 0D; 
        for(int i=0; i<mNumPuntos; i++) {
            sumX = sumX + mPointX[i];
        }
        return sumX;
    }

    /**
     * @return Suma de los datos y
     */
    private double getSumY() {
        double sumY = 0D;
        for(int i=0; i<mNumPuntos; i++) {
            //double newY = mPointY[i];
            sumY = sumY + mPointY[i];
        }
        return sumY;
    }

    /**
     * @return Suma de los cuadrados de los datos y
     */
    private double getSumYY() {
        double sumYY = 0D;
        for(int i=0; i<mNumPuntos; i++) {
            sumYY = sumYY + mPointY[i]*mPointY[i];
        }
        return sumYY;
    }

    /**
     * @return Media de los datos x
     */
    private double getMeanX() {
        double meanX = 0D;
        meanX = getSumX()/mNumPuntos;
        return meanX;
    }

    /**
     * @return Media de los datos y
     */
    private double getMeanY() {
        double meanY = 0D;
        meanY = getSumY()/mNumPuntos;
        return meanY;
    }

    /**
     * @return Desviación estándar de los datos x
     */
    private double getSDXN() {
        double sdxn = 0D;
        sdxn = Math.sqrt((mNumPuntos*getSumXX()-(Math.pow(getSumX(),2)))/(Math.pow(mNumPuntos,2)));
        return sdxn;
    }

    /**
     * @return Desviación estándar de los datos y
     */
    private double getSDYN() {
        double sdyn = 0D;
        sdyn = Math.sqrt((mNumPuntos*getSumYY()-(Math.pow(getSumY(),2)))/(Math.pow(mNumPuntos,2)));
        return sdyn;
    }

    /**
     * @return Desviación estándar de la muestra de los datos x
     */
    private double getSDX() {
        double sdx = 0D;
        sdx = Math.sqrt((mNumPuntos*getSumXX()-(Math.pow(getSumX(),2)))/(mNumPuntos*(mNumPuntos-1)));
        return sdx;
    }

    /**
     * @return Desviación estándar de la muestra de los datos y
     */
    private double getSDY() {
        double sdy = 0D;
        sdy = Math.sqrt((mNumPuntos*getSumYY()-(Math.pow(getSumY(),2)))/(mNumPuntos*(mNumPuntos-1)));
        return sdy;
    }

    /**
     * @return Suma de los productos de los datos x por los datos y
     */
    private double getSumXY() {
        double sumXY = 0D;
        for(int i=0; i<mNumPuntos; i++) {
            //double newXY = mPointX[i]*mPointY[i];
            sumXY = sumXY + mPointX[i]*mPointY[i];
        }
        return sumXY;
    }

    /**
     * @return Suma de los cuadrados de los datos x
     */
    private double getSumXX() {
        double sumXX = 0D;
        for(int i=0; i<mNumPuntos; i++) {
            sumXX = sumXX + mPointX[i]*mPointX[i];
        }
        return sumXX;
    }

    /**
     * Coeficiente de regresión lineal (pendiente de la recta)
     * Ejecutar este método siempre antes que setConstante()
     */
    private void setPendiente() {
        int n = mNumPuntos;
        double pte = 0D;
        pte = (n*getSumXY()-getSumX()*getSumY())/(n*getSumXX()-Math.pow(getSumX(),2));
        mPendiente = pte;
    }

    /**
     * Coeficiente constante de regresión lineal
     * Antes de ejecutar este método debe ser llamado el método setPendiente()
     */
    private void setConstante() {
        int n = mNumPuntos;
        double cte = 0D;
        double pte = mPendiente;
        cte = (getSumY()-pte*getSumX())/(n);
        mConstante = cte;
    }

    /**
     * Coeficiente de correlación
     */
    private void setCorrelacion() {
        int n = mNumPuntos;
        double cor = 0D;
        double num = 0D;
        double denom = 0D;
        double denom1 = 0D;
        double denom2 = 0D;

        num = n * getSumXY()-getSumX()*getSumY();
        denom1 = n*getSumXX()-Math.pow(getSumX(),2);
        denom2 = n*getSumYY()-Math.pow(getSumY(),2);
        denom = Math.sqrt(denom1*denom2);
        cor = num/denom;

        mCorrelacion = cor;
    }
    
    /**
     * R2 -> R cuadrado, que es la raiz del coeficiente de correlacion.
     * Antes de ejecutar este método debe ser llamado el método setCorrelacion()
     * 
     * El coeficiente de correlación miden el grado de relación entre variables aleatorias,
     * y el coeficiente de determinación mide el grado de relación entre una variable aleatoria con una variable fija. 
     */
    private void setRcuadrado() {
        mRcuadrado = Math.pow(mCorrelacion,2);
    }
}
