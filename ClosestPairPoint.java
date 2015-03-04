import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

//for test
import java.util.Arrays;
import java.util.Random;

public class ClosestPairPoint
{
  public static void main(String[] args)
  {
    List<Point> points1 = generatePoints(0, 5, 2);
    test(points1);

    List<Point> points2 = generatePoints(0, 5, 3);
    test(points2);

    List<Point> points3 = generatePoints(0, 5, 4);
    test(points3);

    List<Point> points4 = generatePoints(-5, 5, 10);
    System.out.println("Point List:");
    System.out.println(Arrays.toString(points4.toArray()));

    test(points4);
    List<Point> points5 = generatePoints(-100000, 1000000, 10000);
    test(points5);
  }

  public static void test(List<Point> points)
  {
    Pair expected = bruteForce(points);
    Pair result = findClosestPair(points);

    if(expected.getDistance() != result.getDistance())
    {
      System.out.println("Test Failed");
      System.out.println("Expected: " + expected.toString());
      System.out.println("result: " + result.toString());

      System.out.println("Point List:");
      System.out.println(Arrays.toString(points.toArray()));
    }
  }

  public static List<Point> generatePoints(double min, double max, int pointSize)
  {
    double x, y;
    Point point;
    List<Point> points = new ArrayList<>();

    for(int i = 0; i < pointSize; i++)
    {
      x = randomDoulbe(min, max);
      y = randomDoulbe(min, max);
      point = new Point(x, y);
      points.add(point);
    }
    return points;
  }

  public static double randomDoulbe(double min, double max)
  {
    Random randomGenerator = new Random();
    return (max - min) * randomGenerator.nextDouble() + min;
  }

  public static class Point
  {
    private double x;
    private double y;

    public Point(double x, double y)
    {
      this.x = x;
      this.y = y;
    }

    public double getX()
    {
      return x;
    }

    public double getY()
    {
      return y;
    }

    public String toString()
    {
      return "x = " + x + ", y = " + y;
    }
  }

  public static class Pair
  {
    private Point point1;
    private Point point2;
    private double distance;

    public Pair()
    {
        this.point1 = null;
        this.point2 = null;
        this.distance = Double.NaN;
    }

    public Pair(Point point1, Point point2)
    {
      update(point1, point2);
    }

    public void update(Point point1, Point point2)
    {
      this.point1 = point1;
      this.point2 = point2;
      this.calcDistance();
    }

    public void calcDistance()
    {
      this.distance = ClosestPairPoint.calcDistance(this.point1, this.point2);
    }

    public double getDistance()
    {
      return this.distance;
    }

    public String toString()
    {
      StringBuilder strBuilder = new StringBuilder();
      strBuilder.append("Point1{");
      strBuilder.append(this.point1.toString());
      strBuilder.append("}");
      strBuilder.append(", ");
      strBuilder.append("Point2{");
      strBuilder.append(this.point2.toString());
      strBuilder.append("}");
      return strBuilder.toString();
    }
  }

  public static double calcDistance(Point point1, Point point2)
  {
    double distX = point1.getX() - point2.getX();
    double distY = point1.getY() - point2.getY();
    return Math.hypot(distX, distY);
  }

  public static void sortByX(List<Point> points)
  {
    Collections.sort(points, (Point point1, Point point2) ->
                            (int)Math.signum(point1.getX() - point2.getX())
                    );
  }

  public static void sortByY(List<Point> points)
  {
    Collections.sort(points, (Point point1, Point point2) ->
                            (int)Math.signum(point1.getY() - point2.getY())
                    );
  }

  public static Pair findClosestPair(List<Point> points)
  {
    List<Point> pointSortedByY = new ArrayList<>(points);
    sortByX(points);
    sortByY(pointSortedByY);
    return divideAndConquer(points, pointSortedByY);
  }

  public static Pair bruteForce(List<Point> points)
  {
    int pointSize = points.size();
    if(pointSize < 2)
    {
      System.out.println("ERROR: cannot find cloest pair if points size is smaller than 2");
      return null;
    }

    Pair closestPair = new Pair(points.get(0), points.get(1));
    Point point1, point2;

    for(int i = 0; i < pointSize-1; i++)
    {
      point1 = points.get(i);
      for(int j=i+1; j < pointSize; j++)
      {
        point2 = points.get(j);
        if(closestPair.getDistance() > calcDistance(point1, point2))
        {
          closestPair.update(point1, point2);
        }
      }
    }

    return closestPair;
  }

  private static Pair divideAndConquer(List<Point> pointSortedByX, List<Point> pointSortedByY)
  {
    Pair closestPair = null;
    if(pointSortedByX.size() <= 3)
    {
      closestPair = bruteForce(pointSortedByX);
    }
    else
    {
      int pointSize = pointSortedByX.size();
      int middleXIndex = pointSize / 2;

      List<Point> pointsLeft = new ArrayList<>(pointSortedByX.subList(0, middleXIndex));
      List<Point> pointsRight = new ArrayList<>(pointSortedByX.subList(middleXIndex, pointSize));

      List<Point> tempList = new ArrayList<>(pointsLeft);
      ClosestPairPoint.sortByY(tempList);
      closestPair = divideAndConquer(pointsLeft, tempList);

      tempList.clear();
      tempList.addAll(pointsRight);
      ClosestPairPoint.sortByY(tempList);
      Pair closestPairRight = divideAndConquer(pointsRight, tempList);

      if(closestPair.getDistance() > closestPairRight.getDistance())
        closestPair = closestPairRight;

      tempList = findPointsWithinStrip(pointSortedByY, pointSortedByX.get(middleXIndex).getX(), closestPair.getDistance());
      Pair closestPairSplit = findCloestPointInStrip(tempList, closestPair.getDistance());
      if( closestPairSplit != null && closestPair.getDistance() > closestPairSplit.getDistance())
        closestPair = closestPairSplit;
    }
    return closestPair;
  }

  private static Pair findCloestPointInStrip(List<Point> pointsInStripSortedByY, double stripWidth)
  {
    int pointSize = pointsInStripSortedByY.size();
    if(pointSize < 2)
    {
      return null;
    }

    Pair closestPair = new Pair(pointsInStripSortedByY.get(0), pointsInStripSortedByY.get(1));
    Point point1, point2;
    for(int i = 0; i < pointSize-1; i++)
    {
      point1 = pointsInStripSortedByY.get(i);
      for(int j = i+1; j < pointSize; j++)
      {
        point2 = pointsInStripSortedByY.get(j);
        if(point2.getY() - point1.getY() > stripWidth)
          break;
        if(calcDistance(point1, point2) < closestPair.getDistance())
        {
          closestPair.update(point1, point2);
        }
      }
    }
    return closestPair;
  }

  private static List<Point> findPointsWithinStrip(List<Point> points, final double middleX, final double closestDistance)
  {
    return points.stream()
            .filter(point -> Math.abs(point.getX() - middleX) > closestDistance)
            .collect(Collectors.toList());
  }
}
