package Lab_03;

public class Main {

  public static double sinLib(double x) {
    return Math.sin(x);
  }

  public static double sinTaylor(double x) {
    x = x % (2 * Math.PI);
    double term = x;
    double sum = x;
    for (int n = 1; n < 10; n++) {
      term *= -1 * x * x / ((2 * n) * (2 * n + 1));
      sum += term;
    }
    return sum;
  }


  public static double sinProstokaty(double x) {
    int n = 10000;
    double a = 0.0;
    double b = x;
    double h = (b - a) / n;
    double sum = 0.0;
    for (int i = 0; i < n; i++) {
      double t = a + i * h;
      sum += Math.tan(t) * h; // pozostawiony blad - tangenst zamiast cosinusa
    }
    return sum;
  }

  public static Double majorityVote(double v1, double v2, double v3) {
    double eps = 0.001;
    if (Math.abs(v1 - v2) < eps)
      return v1;
    if (Math.abs(v1 - v3) < eps)
      return v1;
    if (Math.abs(v2 - v3) < eps)
      return v2;
    return null;

  }

  public static void main(String[] args) {
    double[] testy = {0, Math.PI, Math.PI/2, 2*Math.PI, Math.PI/3, 3*Math.PI};

    for (double x : testy) {
      double v1 = sinLib(x);
      double v2 = sinTaylor(x);
      double v3 = sinProstokaty(x);

      Double wybrany_wynik = majorityVote(v1, v2, v3);

      System.out.printf("x = %f%n", x);
      System.out.printf("Math.sin: %f%n", v1);
      System.out.printf("Taylor: %f%n", v2);
      System.out.printf("Prostokty z bledem: %f%n", v3);
      if (wybrany_wynik != null)
        System.out.printf("Wynik po glosowaniu: %f%n", wybrany_wynik);
      else
        System.out.println("Brak zgodnosci miedzy wersjami");
      System.out.println();
    }
  }
}
