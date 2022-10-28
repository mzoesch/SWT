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

public class NavigationL {

  public static void main(String[] args) {
    new NavigationL().computeNavigation();
  }

  private void computeNavigation() {
    try {
      Region region = readRegionFromFile();
      City startCity = getCityByName("n0", region);
      City targetCity = getCityByName("n1", region);

      // NOTE: We achieve better abstraction separation by not passing Strings into
      // this method.
      // It now only has to deal with "fully parsed" input (i.e. resolved to Java
      // objects)
      List<City> shortestPath = computeShortestPathBetweenCities(startCity, targetCity, region);

      System.out.println(shortestPath);
    } catch (IOException e) {
      System.out.println("ERROR");
    }
  }

  private static City getCityByName(String cityName, Region region) {
    // NOTE: By making this method more general (finding the city for any name
    // instead of only
    // start, end) we also simplify it.
    for (City city : region.cities) {
      if (cityName.equals(city.name)) {
        return city;
      }
    }
    return null;
  }

  private List<City> computeShortestPathBetweenCities(City start, City target, Region region) {
    Map<City, List<RoutingEntry>> neighborDistances = computeNeighborDistances(region);
    Map<City, City> predecessorsOnShortestPath = computePredecessorsOnShortestPath(start, neighborDistances);
    return extractPathFromPredecessorMap(predecessorsOnShortestPath, target);
  }

  private List<City> extractPathFromPredecessorMap(Map<City, City> predecessors, City target) {
    List<City> extractedPath = new ArrayList<>();
    City currentCity = target;
    extractedPath.add(currentCity);
    // The root in the predecessors map has itself as predecessor.
    while (!currentCity.equals(predecessors.get(currentCity))) {
      currentCity = predecessors.get(currentCity);
      extractedPath.add(currentCity);
    }
    Collections.reverse(extractedPath);

    return extractedPath;
  }

  private Map<City, List<RoutingEntry>> computeNeighborDistances(Region region) {
    Map<City, List<RoutingEntry>> neighbors = new HashMap<>();
    // First we add all the cities to the map
    for (City city : region.cities) {
      neighbors.put(city, new ArrayList<>());
    }

    // Then we add neighbors according to the edges
    for (Street street : region.streets) {
      // Streets are bidirectional, thus we add the entry to both ends of the street.
      neighbors.get(street.from).add(new RoutingEntry(street.to, street.length));
      neighbors.get(street.to).add(new RoutingEntry(street.from, street.length));
    }

    return neighbors;
  }

  // NOTE: Here we have a better separation of abstractions, because this method
  // deals with graphs
  // while the calling method considers regions and doesn't really know anything
  // about graphs.
  private Map<City, City> computePredecessorsOnShortestPath(
      City start, Map<City, List<RoutingEntry>> adjacencyLists) {
    Map<City, Integer> distances = getInfiniteDistanceMap(adjacencyLists.keySet());
    distances.put(start, 0);

    Map<City, City> predecessors = new HashMap<>();
    predecessors.put(start, start);

    Queue<RoutingEntry> minDistanceCities = new PriorityQueue<>(adjacencyLists.size(),
        Comparator.comparingInt(r -> r.distance));
    minDistanceCities.add(new RoutingEntry(start, 0));
    Set<City> visitedCities = new HashSet<>();

    // Dijkstra's algorithm
    while (!minDistanceCities.isEmpty()) {
      City current = minDistanceCities.remove().city;
      if (visitedCities.contains(current)) {
        continue;
      }
      visitedCities.add(current);
      for (RoutingEntry neighbor : adjacencyLists.get(current)) {
        // NOTE: We extract variables to make it easier to understand the if-condition.
        int fromCurrentToNeighbor = distances.get(current) + neighbor.distance;
        int shortestDistanceToNeighbor = distances.get(neighbor.city);

        if (fromCurrentToNeighbor < shortestDistanceToNeighbor) {
          distances.put(neighbor.city, fromCurrentToNeighbor);
          predecessors.put(neighbor.city, current);
          minDistanceCities.add(new RoutingEntry(neighbor.city, fromCurrentToNeighbor));
        }
      }
    }

    return predecessors;
  }

  private static <T> Map<T, Integer> getInfiniteDistanceMap(Set<T> nodes) {
    final int infinity = Integer.MAX_VALUE;
    Map<T, Integer> distances = new HashMap<>();
    for (T node : nodes) {
      distances.put(node, infinity);
    }

    return distances;
  }

  // NOTE this method is likely best refactored by moving it into a separate
  // class.
  // E.g. moving the body of the while loop to a separate method would require us
  // to pass cities and
  // streets to that method. It would be better to have them as fields in an
  // InputReader class.
  private Region readRegionFromFile() throws IOException {
    Map<String, City> cities = new HashMap<>();
    Set<Street> streets = new HashSet<>();
    boolean expectCityEntries = false;
    boolean expectStreetEntries = false;
    try (BufferedReader br = new BufferedReader(new FileReader("input.txt"))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("cities")) {
          expectCityEntries = true;
          expectStreetEntries = false;
        } else if (line.startsWith("streets")) {
          expectCityEntries = false;
          expectStreetEntries = true;
        } else if (expectCityEntries) {
          City parsedCity = parseCityDescription(line);
          cities.put(parsedCity.name, parsedCity);
        } else if (expectStreetEntries) {
          Street parsedStreet = parseStreetDescription(line, cities);
          streets.add(parsedStreet);
        }
        // TODO here we can handle if neither cities nor streets are expected and throw
        // an exception
      }

      return new Region(new HashSet<>(cities.values()), streets);
    }
  }

  private City parseCityDescription(String description) {
    return new City(description.trim());
  }

  private Street parseStreetDescription(String description, Map<String, City> citiesByName) {
    String[] split = description.trim().split(" ");
    City from = citiesByName.get(split[0]);
    City to = citiesByName.get(split[1]);
    int streetLength = Integer.parseInt(split[2]);

    from.neighbors.add(to);
    to.neighbors.add(from);

    return new Street(from, to, streetLength);
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
