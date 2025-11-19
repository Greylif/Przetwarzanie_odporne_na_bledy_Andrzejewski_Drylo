package Lab_03;

public class Main {

  public static double sinLibrary(double x) {

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


  public static double sinRectangles(double x) {
    int n = 10000;
    double a = 0.0;
    double b = x;
    double h = (b - a) / n;
    double sum = 0.0;
    for (int i = 0; i < n; i++) {
      double t = a + i * h;
      sum += Math.cos(t) * h; // pozostawiony blad - znak "-" przed cos(t)
    }
    return sum;
  }

  public static Double majorityVote(double v1, double v2, double v3) {
    double eps = 1e-4;
    if (Math.abs(v1 - v2) < eps && Math.abs(v1 - v3) < eps)
      return v1;
    if (Math.abs(v1 - v2) < eps)
      return v1;
    if (Math.abs(v1 - v3) < eps)
      return v1;
    if (Math.abs(v2 - v3) < eps)
      return v2;
    return null;
  }

  public static void main(String[] args) {
    double[] testValues = {0, Math.PI/6, Math.PI/2, Math.PI, 3*Math.PI/2};

    for (double x : testValues) {
      double v1 = sinLibrary(x);
      double v2 = sinTaylor(x);
      double v3 = sinRectangles(x);

      Double voted = majorityVote(v1, v2, v3);

      System.out.printf("x = %.4f%n", x);
      System.out.printf("Wersja 1 Math.sin: %.6f%n", v1);
      System.out.printf("Wersja 2 Taylor: %.6f%n", v2);
      System.out.printf("Wersja 3 Prostokty z bledem: %.6f%n", v3);
      if (voted != null)
        System.out.printf("Wynik po glosowaniu: %.6f%n", voted);
      else
        System.out.println("Brak zgodnosci miedzy wersjami");
      System.out.println();
    }
  }
}
