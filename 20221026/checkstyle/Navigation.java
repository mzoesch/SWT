import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class Navigation {

  public static void main(String[] args) {
    new Navigation().go();
  }

  private void go() {
    Region region = readInput();
    if(region == null) {
      // Region not present
      System.out.println("ERROR");
    } else {
      // We know the region
      System.out.println(solvingMethod("n0", "n1", region));
    }
  }

  private List<City> solvingMethod(String start, String end, Region region) {
    // determine s1 and s2
    City s1 = null;
    City s2 = null;
    for (City city : region.cities) {
      if (start.equals(city.name)) {
        s1 = city;
      } else if (end.equals(city.name)) {
        s2 = city;
      }
    }
    // initialize d
    Map<City, Integer> d = new HashMap<>();
    for (City city : region.cities) {
      d.put(city, Integer.MAX_VALUE);
    }
    d.put(s1, 0);
    Map<City, City> predecessor = new HashMap<>();
    predecessor.put(s1, s1);
    // Queue to sort the cities by distance
    Queue<RoutingEntry> queue =
        new PriorityQueue<>(region.cities.size(), Comparator.comparingInt(r -> r.distance));
    queue.add(new RoutingEntry(s1, 0));
    Set<City> visited = new HashSet<>();
    // Build a map that stores the neighbors and distances to them for each city.
    Map<City, List<RoutingEntry>> neighbors = new HashMap<>();
    for (Street street : region.streets) {
      if (!neighbors.containsKey(street.from)) {
        neighbors.put(street.from, new ArrayList<>());
      }
      if (!neighbors.containsKey(street.to)) {
        neighbors.put(street.to, new ArrayList<>());
      }
      // Streets a bidirectional
      neighbors.get(street.from).add(new RoutingEntry(street.to, street.length));
      neighbors.get(street.to).add(new RoutingEntry(street.from, street.length));
    }
    // Dijkstra's algorithm
    while (!queue.isEmpty()) {
      City current = queue.remove().city;
      if (visited.contains(current)) {
        continue;
      }
      visited.add(current);
      for (RoutingEntry neighbor : neighbors.get(current)) {
        if (d.get(current) + neighbor.distance < d.get(neighbor.city)) {
          d.put(neighbor.city, d.get(current) + neighbor.distance);
          predecessor.put(neighbor.city, current);
          queue.add(new RoutingEntry(neighbor.city, d.get(neighbor.city)));
        }
      }
    }
    List<City> result = new ArrayList<>();
    City last = s2; // last is the last city ...
    result.add(last); // ... and the first in the result
    while (!last.equals(predecessor.get(last))) {
      last = predecessor.get(last);
      result.add(last);
    }
    Collections.reverse(result);
    return result;
  }

  private Region readInput() {
    Map<String, City> cities = new HashMap<>();
    Set<Street> streets = new HashSet<>();
    boolean readCities = false; // whether we should not already parse the streets
    try (BufferedReader br = new BufferedReader(new FileReader("input.txt"))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("cities")) {
          readCities = true;
          continue;
        }

        if (line.startsWith("streets")) {
          readCities = false;
          continue;
        }

        if (readCities) {
          cities.put(line.trim(), new City(line.trim()));
        } else {
          String[] split = line.trim().split(" ");
          City from = cities.get(split[0]);
          City to = cities.get(split[1]);
          int streetLength = Integer.parseInt(split[2]);
          streets.add(new Street(from, to, streetLength));

          from.neighbors.add(to);
          to.neighbors.add(from);
        }
      }

      return new Region(new HashSet<>(cities.values()), streets);
      // TODO consider using this:
      // return new Region(new TreeSet<>(cities.values()), streets);
    } catch(IOException e) {
      return null;
    }
  }

  private static class City {

    String name;
    List<City> neighbors;

    private City(String name) {
      this.name = name;
      neighbors = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof City)) {
        return false;
      }
      City city = (City) o;
      return Objects.equals(name, city.name) && Objects.equals(neighbors, city.neighbors);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }

    @Override
    public String toString() {
      return name;
    }
  }

  private static class Street {

    City from;
    City to;
    int length;

    private Street(City from, City to, int length) {
      this.from = from;
      this.to = to;
      this.length = length;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Street)) {
        return false;
      }
      Street street = (Street) o;
      return Objects.equals(from, street.from)
          && Objects.equals(to, street.to)
          && Objects.equals(length, street.length);
    }

    @Override
    public int hashCode() {
      return Objects.hash(from, to, length);
    }

    @Override
    public String toString() {
      return from + " -" + length + "-> " + to;
    }
  }

  private static class Region {

    Set<City> cities;
    Set<Street> streets;

    private Region(Set<City> cities, Set<Street> streets) {
      this.cities = cities;
      this.streets = streets;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Region)) {
        return false;
      }
      Region region = (Region) o;
      return Objects.equals(cities, region.cities) && Objects.equals(streets, region.streets);
    }

    @Override
    public int hashCode() {
      return Objects.hash(cities, streets);
    }

    @Override
    public String toString() {
      return "Region{" + "cities=" + cities + ", streets=" + streets + '}';
    }
  }

  private static class RoutingEntry {

    City city;
    int distance;

    private RoutingEntry(City city, int distance) {
      this.city = city;
      this.distance = distance;
    }
  }
}
