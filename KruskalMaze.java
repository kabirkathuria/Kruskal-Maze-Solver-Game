// Instructions:
//
// - Press "B" to search the maze with breadth-first search
// - Press "D" to search the maze with depth-first search
// - Press "R" to reset the maze to the same maze you just completed
// - Press "N" to create a new maze
// - Press "H" to create a new horizontally-biased maze
// - Press "V" to create a new vertically-biased maze
// - Press "P" to pause the game while traversing through the maze
//
// When you complete the maze, it should return the total number of vertices searched, 
// the number of vertices in the correct path, and the wrong steps taken when traversing 
// through the maze. It should also return the total time taken to complete the maze.

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Random;
import java.util.LinkedList;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// to represent spaces in a maze
class Vertex {

  // coordinates with the origin at the top-left corner of the screen
  int x;

  int y;

  // color of this Vertex
  Color color;

  // constructor for this Vertex
  Vertex(int x, int y, Color color) {
    this.x = x;
    this.y = y;
    this.color = color;
  }

  // convenience constructor for this Vertex colors it gray by default
  Vertex(int x, int y) {
    this.x = x;
    this.y = y;
    this.color = Color.GRAY;
  }

  // define a custom hashCode for this Vertex
  @Override
  public int hashCode() {
    return this.x * 10000 + this.y;
  }

  // override equals method for Vertex to check if this Vertex is the same as the
  // given object
  @Override
  public boolean equals(Object given) {
    if (!(given instanceof Vertex)) {
      return false;
    }

    // this cast is safe, because we just checked instanceof
    Vertex that = (Vertex) given;
    return this.x == that.x && this.y == that.y;
  }

  // EFFECT: draws this Vertex on the given WorldScene
  void drawVertex(WorldScene scene) {
    scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, this.color),
        110 + this.x * 20 + 10, 110 + this.y * 20 + 10);
  }

  // EFFECT: draws the wall between this Vertex and the given Vertex
  void drawWallBetween(Vertex given, WorldScene scene) {
    if (this.x == given.x) {
      scene.placeImageXY(new RectangleImage(20, 2, OutlineMode.SOLID, Color.BLACK),
          110 + this.x * 20 + 10, 110 + (this.y + 1) * 20);
    }
    else {
      scene.placeImageXY(new RectangleImage(2, 20, OutlineMode.SOLID, Color.BLACK),
          110 + (this.x + 1) * 20, 110 + this.y * 20 + 10);
    }
  }

  // EFFECT: changes the Color of this Vertex to the given Color
  void changeColor(Color color) {
    this.color = color;
  }
}

// to represent connections between vertices
class Edge {

  // used to implement Kruskal's algorithm
  int weight;

  // the Vertex this Edge extends from
  Vertex from;

  // the Vertex this Edge connects to
  Vertex to;

  // constructor for this Edge
  Edge(int weight, Vertex from, Vertex to) {
    this.weight = weight;
    this.from = from;
    this.to = to;
  }

  // define a custom hashCode for this Edge
  @Override
  public int hashCode() {
    return this.weight * 100000000 + this.from.hashCode() + this.to.hashCode();
  }

  // override equals method for Edge to check if this Edge is the same as the
  // given object
  @Override
  public boolean equals(Object given) {

    if (!(given instanceof Edge)) {
      return false;
    }

    // this cast is safe, because we just checked instanceof
    Edge that = (Edge) given;

    return this.weight == that.weight && this.from.equals(that.from) && this.to.equals(that.to);
  }

  // return the Vertex this Edge extends from
  Vertex getFrom() {
    return this.from;
  }

  // return the Vertex this Edge connects to
  Vertex getTo() {
    return this.to;
  }

}

// compares Edges by weight
class EdgesByWeight implements Comparator<Edge> {

  // override compare method to compare Edges by weight
  @Override
  public int compare(Edge e1, Edge e2) {
    return e1.weight - e2.weight;
  }
}

// Represents a mutable collection of items
interface ICollection<T> {

  // Is this collection empty?
  boolean isEmpty();

  // EFFECT: adds the item to the collection
  void add(T item);

  // Returns the first item of the collection
  // EFFECT: removes that first item
  T remove();
}

// to represent a FIFO list
class Stack<T> implements ICollection<T> {

  // to store items in this Stack
  Deque<T> contents;

  // constructor for an empty Stack
  Stack() {
    this.contents = new LinkedList<T>();
  }

  // convenience constructor for this Stack that takes in a list of items
  Stack(LinkedList<T> contents) {
    this.contents = contents;
  }

  // returns true if this Stack is empty
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  // adds the given item to the top of the Stack
  public void add(T item) {
    this.contents.addFirst(item);
  }

  // returns the first item of this Stack
  // EFFECT: removes the first item
  public T remove() {
    return this.contents.removeFirst();
  }
}

// to represent a LIFO list
class Queue<T> implements ICollection<T> {

  // to store items in this Queue
  Deque<T> contents;

  // constructor for an empty Queue
  Queue() {
    this.contents = new LinkedList<T>();
  }

  // convenience constructor for this Queue that takes in a list of items
  Queue(LinkedList<T> contents) {
    this.contents = contents;
  }

  // returns true if this Stack is empty
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  // adds the given item to the end of this Queue
  public void add(T item) {
    this.contents.addLast(item);
  }

  // returns the first item of this Stack
  // EFFECT: removes the first item
  public T remove() {
    return this.contents.removeFirst();
  }
}

// to represent a maze
class MazeWorld extends World {

  // used to keep track of how much time has passed
  int time;

  // to display game title
  TextImage title = new TextImage("Prepare to Be A-mazed", 20, FontStyle.BOLD, Color.BLACK);

  // to display game instructions
  TextImage instructions1 = new TextImage("Press B to search the maze with breadth-first search",
      10, Color.BLACK);

  // to display more game instructions
  TextImage instructions2 = new TextImage("and D to search the maze with depth-first search.", 10,
      Color.BLACK);

  // to display game reset instructions
  TextImage resetInfo = new TextImage("Press R to reset the game, N to create a new maze,", 10,
      Color.BLACK);

  // to display more game reset instructions
  TextImage resetInfo2 = new TextImage("H to create a new horizontally-biased maze,", 10,
      Color.BLACK);

  // to display even more game reset instructions
  TextImage resetInfo3 = new TextImage("or V to create a new vertically-biased maze.", 10,
      Color.BLACK);

  // to display which search is currently being used
  TextImage mode = new TextImage("", 15, Color.BLACK);

  // to display whether or not the game is currently paused
  TextImage pause = new TextImage("", 15, Color.BLACK);

  // length of the maze
  int length;

  // width of the maze
  int width;

  // to randomly assign edge weights
  Random rand;

  // to represent all the spaces in the maze
  ArrayList<ArrayList<Vertex>> maze;

  // to represent all the Edges between vertices in the maze
  ArrayList<Edge> edges;

  // to associate each Vertex in the maze with a representative element Vertex
  HashMap<Vertex, Vertex> representatives;

  // to represent minimum spanning tree for the maze
  ArrayList<Edge> mst;

  // to draw the game
  WorldScene scene;

  // to indicate whether or not the maze is currently being searched
  boolean searching;

  // to indicate whether or not the search is paused
  boolean paused;

  // to indicate whether or not the maze solution has been found
  boolean finished;

  // to keep track of which vertices still have to be searched
  ICollection<Vertex> worklist;

  // to keep track of which vertices have already been searched
  Deque<Vertex> alreadySeen;

  // to keep track of which vertices that have already been searched need to be
  // colored
  Deque<Vertex> alreadySeen2;

  // to keep track of vertices and edges needed to reconstruct the correct path
  HashMap<Vertex, Edge> cameFromEdge;

  // to represent the correct path from the start of the maze to the end
  ArrayList<Vertex> path;

  // to keep track of which vertices in the correct path need to be colored
  ArrayList<Vertex> path2;

  // constructor for this MazeWorld
  MazeWorld(int length, int width) {
    this.length = length;
    this.width = width;
    this.rand = new Random();
    this.maze = new ArrayList<ArrayList<Vertex>>();
    this.edges = new ArrayList<Edge>();
    this.representatives = new HashMap<Vertex, Vertex>();
    this.scene = new WorldScene(220 + 20 * this.length, 220 + 20 * this.width);
    this.generateMaze();
    this.assignEdges();
    this.mst = this.kruskal();
    this.time = 0;
    this.searching = false;
    this.paused = false;
    this.finished = false;
    this.alreadySeen = new LinkedList<Vertex>();
    this.alreadySeen2 = new LinkedList<Vertex>();
    this.cameFromEdge = new HashMap<Vertex, Edge>();
    this.path = new ArrayList<Vertex>();
    this.path2 = new ArrayList<Vertex>();
  }

  // constructor for this MazeWorld with seeded random for testing
  MazeWorld(int length, int width, Random rand) {
    this.length = length;
    this.width = width;
    this.rand = rand;
    this.maze = new ArrayList<ArrayList<Vertex>>();
    this.edges = new ArrayList<Edge>();
    this.representatives = new HashMap<Vertex, Vertex>();
    this.scene = new WorldScene(220 + 20 * this.length, 220 + 20 * this.width);
    this.generateMaze();
    this.assignEdges();
    this.mst = this.kruskal();
    this.time = 0;
    this.searching = false;
    this.paused = false;
    this.finished = false;
    this.alreadySeen = new LinkedList<Vertex>();
    this.alreadySeen2 = new LinkedList<Vertex>();
    this.cameFromEdge = new HashMap<Vertex, Edge>();
    this.path = new ArrayList<Vertex>();
    this.path2 = new ArrayList<Vertex>();
  }

  // EFFECT: fills this MazeWorld's board with vertices and
  // associates each Vertex with itself in this MazeWorld's representatives
  // HashMap
  void generateMaze() {
    for (int i = 0; i < this.width; i++) {
      this.maze.add(i, new ArrayList<Vertex>());
      for (int j = 0; j < this.length; j++) {
        if (i == 0 && j == 0) {
          this.maze.get(i).add(new Vertex(j, i, Color.GREEN));
          this.representatives.put(this.maze.get(i).get(j), this.maze.get(i).get(j));
        }
        else if (i == this.width - 1 && j == this.length - 1) {
          this.maze.get(i).add(new Vertex(j, i, Color.RED));
          this.representatives.put(this.maze.get(i).get(j), this.maze.get(i).get(j));
        }
        else {
          this.maze.get(i).add(new Vertex(j, i));
          this.representatives.put(this.maze.get(i).get(j), this.maze.get(i).get(j));
        }
      }
    }
  }

  // EFFECT: adds Edges of unique weights connecting this MazeWorld's vertices
  // to their bottom and right neighbors if they have them
  void assignEdges() {
    ArrayList<Integer> weights = new ArrayList<Integer>();
    for (int a = 0; a < this.length * (this.width - 1) + (this.length - 1) * this.width; a++) {
      weights.add(a);
    }
    for (int i = 0; i < this.width; i++) {
      for (int j = 0; j < this.length; j++) {
        if (j < this.length - 1) {
          int idx = this.rand.nextInt(weights.size());
          Edge r = new Edge(weights.get(idx), this.maze.get(i).get(j), this.maze.get(i).get(j + 1));
          weights.remove(idx);
          this.edges.add(r);
        }
        if (i < this.width - 1) {
          int idx = this.rand.nextInt(weights.size());
          Edge b = new Edge(weights.get(idx), this.maze.get(i).get(j), this.maze.get(i + 1).get(j));
          weights.remove(idx);
          this.edges.add(b);
        }
      }
    }
  }

  // EFFECT: adds Edges of unique weights connecting this MazeWorld's vertices
  // to their bottom and right neighbors if they have them
  // if the given boolean is true,
  // gives horizontal Edges lower weights so they have a better chance of getting
  // added to the MST
  // therefore constructing a maze with a preference for horizontal corridors
  // if the given boolean is false,
  // gives vertical Edges lower weights so they have a better chance of getting
  // added to the MST
  // therefore constructing a maze with a preference for vertical corridors
  void assignEdgesBias(boolean bias) {
    ArrayList<Integer> weights1 = new ArrayList<Integer>();
    ArrayList<Integer> weights2 = new ArrayList<Integer>();

    if (bias) {
      for (int a = 0; a < (this.length - 1) * this.width; a++) {
        weights1.add(a);
      }
      for (int a = (this.length - 1) * this.width; a < this.length * (this.width - 1)
          + (this.length - 1) * this.width; a++) {
        weights2.add(a);
      }
      for (int i = 0; i < this.width; i++) {
        for (int j = 0; j < this.length; j++) {
          if (j < this.length - 1) {
            int idx = this.rand.nextInt(weights1.size());
            Edge r = new Edge(weights1.get(idx), this.maze.get(i).get(j),
                this.maze.get(i).get(j + 1));
            weights1.remove(idx);
            this.edges.add(r);
          }
          if (i < this.width - 1) {
            int idx = this.rand.nextInt(weights2.size());
            Edge b = new Edge(weights2.get(idx), this.maze.get(i).get(j),
                this.maze.get(i + 1).get(j));
            weights2.remove(idx);
            this.edges.add(b);
          }
        }
      }
    }
    else {
      for (int a = 0; a < (this.width - 1) * this.length; a++) {
        weights1.add(a);
      }
      for (int a = (this.width - 1) * this.length; a < this.length * (this.width - 1)
          + (this.length - 1) * this.width; a++) {
        weights2.add(a);
      }
      for (int i = 0; i < this.width; i++) {
        for (int j = 0; j < this.length; j++) {
          if (j < this.length - 1) {
            int idx = this.rand.nextInt(weights2.size());
            Edge r = new Edge(weights2.get(idx), this.maze.get(i).get(j),
                this.maze.get(i).get(j + 1));
            weights2.remove(idx);
            this.edges.add(r);
          }
          if (i < this.width - 1) {
            int idx = this.rand.nextInt(weights1.size());
            Edge b = new Edge(weights1.get(idx), this.maze.get(i).get(j),
                this.maze.get(i + 1).get(j));
            weights1.remove(idx);
            this.edges.add(b);
          }
        }
      }
    }
  }

  // trace the given Vertex through this MazeWorld's HashMap of representatives
  // to find and return the Vertex it ultimately points to
  Vertex find(Vertex v, ArrayList<Vertex> checked) {
    if (checked.contains(v)) {
      return v;
    }
    else {
      if (v.equals(this.representatives.get(v))) {
        return v;
      }
      else {
        checked.add(v);
        return this.find(this.representatives.get(v), checked);
      }
    }
  }

  // EFFECT: sets the value of one representative’s representative to the other
  void union(Vertex that, Vertex another) {
    this.representatives.put(this.find(another, new ArrayList<Vertex>()),
        this.find(that, new ArrayList<Vertex>()));
  }

  // returns a minimum spanning tree using Kruskal's algorithm on this MazeWorld's
  // maze
  // EFFECT: unions all of the vertices in this MazeWorld to the same
  // representative group
  ArrayList<Edge> kruskal() {
    ArrayList<Edge> worklistEdges = new ArrayList<Edge>(this.edges);
    worklistEdges.sort(new EdgesByWeight());
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
    while (edgesInTree.size() < this.length * this.width - 1) {
      for (Edge e : worklistEdges) {
        if (!this.find(e.getFrom(), new ArrayList<Vertex>())
            .equals(this.find(e.getTo(), new ArrayList<Vertex>()))) {
          edgesInTree.add(e);
          this.union(e.getFrom(), e.getTo());
        }
      }
    }
    return edgesInTree;
  }

  // returns String representation of the elapsed time
  public String getTime() {
    if (!this.finished) {
      return "Time Elapsed: " + Integer.toString(this.time / 20);
    }
    else {
      return "Total Time Taken: " + Integer.toString(this.time / 20);
    }
  }

  // returns WorldScene with all of this MazeWorld's vertices and information
  // drawn on it
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(220 + 20 * this.length, 220 + 20 * this.width);
    scene.placeImageXY(this.title, (220 + 20 * this.length) / 2, 12);
    scene.placeImageXY(this.instructions1, (220 + 20 * this.length) / 2, 30);
    scene.placeImageXY(this.instructions2, (220 + 20 * this.length) / 2, 40);
    scene.placeImageXY(this.resetInfo, (220 + 20 * this.length) / 2, (220 + 20 * this.width) - 30);
    scene.placeImageXY(this.resetInfo2, (220 + 20 * this.length) / 2, (220 + 20 * this.width) - 20);
    scene.placeImageXY(this.resetInfo3, (220 + 20 * this.length) / 2, (220 + 20 * this.width) - 10);
    TextImage timer = new TextImage(this.getTime(), 15, Color.BLACK);
    scene.placeImageXY(timer, (220 + 20 * this.length) / 2, (220 + 20 * this.width) - 50);
    scene.placeImageXY(this.mode, (220 + 20 * this.length) / 2, 60);
    if (this.searching) {
      scene.placeImageXY(this.pause, (200 + 20 * this.length) / 2, 80);
    }

    if (this.finished) {
      TextImage soFar = new TextImage("Total Vertices Searched: " + this.alreadySeen.size(), 15,
          Color.BLACK);
      scene.placeImageXY(soFar, (220 + 20 * this.length) / 2, (220 + 20 * this.width) - 100);
      TextImage stats = new TextImage("The correct path is " + this.path.size() + " vertices long",
          15, Color.BLACK);
      scene.placeImageXY(stats, (220 + 20 * this.length) / 2, (220 + 20 * this.width) - 85);
      TextImage stats2 = new TextImage(
          (this.alreadySeen.size() - this.path.size()) + " wrong steps were taken", 15,
          Color.BLACK);
      scene.placeImageXY(stats2, (220 + 20 * this.length) / 2, (220 + 20 * this.width) - 70);
    }
    else {
      TextImage soFar = new TextImage("Vertices searched so far: " + this.alreadySeen.size(), 15,
          Color.BLACK);
      scene.placeImageXY(soFar, (220 + 20 * this.length) / 2, (220 + 20 * this.width) - 100);
    }

    for (ArrayList<Vertex> row : this.maze) {
      for (Vertex v : row) {
        v.drawVertex(scene);
      }
    }
    for (Edge e : this.edges) {
      if (!this.mst.contains(e)) {
        e.getFrom().drawWallBetween(e.getTo(), scene);
      }
    }

    if (this.alreadySeen2.size() != 0) {
      Vertex v = this.alreadySeen2.removeFirst();
      v.changeColor(Color.CYAN);
    }

    if (this.path2.size() != 0) {
      Vertex v = this.path2.remove(0);
      v.changeColor(Color.BLUE);
    }

    return scene;
  }

  // ticks the clock and updates this MazeWorld
  public void onTick() {

    if (!this.paused) {
      if (this.searching) {

        this.time++;

        if (!this.worklist.isEmpty()) {
          Vertex next = this.worklist.remove();
          if (this.alreadySeen.contains(next)) {
            // do nothing: we've already seen this one
          }
          else if (next.equals(this.maze.get(this.width - 1).get(this.length - 1))) {
            this.alreadySeen.add(next);
            this.alreadySeen2.add(next);
            this.searching = false;
            this.finished = true;
            this.reconstruct(this.maze.get(0).get(0), next);
          }
          else {
            // add all the neighbors of next to the worklist for further processing
            for (Edge e : this.mst) {
              if (e.getFrom().equals(next) && !this.alreadySeen.contains(e.getTo())) {
                this.worklist.add(e.getTo());
                this.cameFromEdge.put(e.getTo(), e);
              }
              else if (e.getTo().equals(next) && !this.alreadySeen.contains(e.getFrom())) {
                this.worklist.add(e.getFrom());
                this.cameFromEdge.put(e.getFrom(), new Edge(
                    e.getTo().hashCode() + e.getFrom().hashCode(), e.getTo(), e.getFrom()));
              }
            }
            this.alreadySeen.addFirst(next);
            this.alreadySeen2.addFirst(next);
          }
        }
      }
    }
  }

  // EFFECT: resets game fields for new mazes or searches
  public void reset() {
    this.time = 0;
    this.alreadySeen = new LinkedList<Vertex>();
    this.alreadySeen2 = new LinkedList<Vertex>();
    this.cameFromEdge = new HashMap<Vertex, Edge>();
    this.path = new ArrayList<Vertex>();
    this.path2 = new ArrayList<Vertex>();
    this.paused = false;
    this.pause = new TextImage("", 15, Color.BLACK);
  }

  // EFFECT: resets game fields for new mazes
  public void newMaze() {
    this.rand = new Random();
    this.maze = new ArrayList<ArrayList<Vertex>>();
    this.edges = new ArrayList<Edge>();
    this.representatives = new HashMap<Vertex, Vertex>();
    this.scene = new WorldScene(220 + 20 * this.length, 220 + 20 * this.width);
    this.generateMaze();
    this.mode = new TextImage("", 15, Color.BLACK);
    this.searching = false;
    this.finished = false;
    this.paused = false;
    this.pause = new TextImage("", 15, Color.BLACK);
  }

  // EFFECT: handles key input
  public void onKeyEvent(String key) {
    // EFFECT: resets the board
    if (key.equals("r")) {
      this.reset();
      this.mode = new TextImage("", 15, Color.BLACK);
      this.searching = false;
      this.finished = false;
      for (ArrayList<Vertex> row : this.maze) {
        for (Vertex v : row) {
          v.changeColor(Color.GRAY);
        }
      }
      this.maze.get(0).get(0).changeColor(Color.GREEN);
      this.maze.get(this.width - 1).get(this.length - 1).changeColor(Color.RED);
    }
    // EFFECT: resets the game (initializes all fields) and creates a new board
    else if (key.equals("n")) {
      this.reset();
      this.newMaze();
      this.assignEdges();
      this.mst = this.kruskal();
    }
    // EFFECT: resets the game (initializes all fields) and creates a new
    // horizontally-biased board
    else if (key.equals("h")) {
      this.reset();
      this.newMaze();
      this.assignEdgesBias(true);
      this.mst = this.kruskal();
    }
    // EFFECT: resets the game (initializes all fields) and creates a new
    // vertically-biased board
    else if (key.equals("v")) {
      this.reset();
      this.newMaze();
      this.assignEdgesBias(false);
      this.mst = this.kruskal();
    }
    // EFFECT: starts breadth-first search on the maze
    // can switch modes mid-search or after the maze has been solved
    else if (key.equals("b")) {
      this.reset();
      this.mode = new TextImage("currently using breadth-first search", 15, Color.BLACK);
      this.scene = new WorldScene(220 + 20 * this.length, 220 + 20 * this.width);
      this.searching = true;
      this.finished = false;
      this.worklist = new Queue<Vertex>();
      this.worklist.add(this.maze.get(0).get(0));
      this.cameFromEdge.put(this.maze.get(0).get(0),
          new Edge(2 * this.maze.get(0).get(0).hashCode(), this.maze.get(0).get(0),
              this.maze.get(0).get(0)));
      for (ArrayList<Vertex> row : this.maze) {
        for (Vertex v : row) {
          v.changeColor(Color.GRAY);
        }
      }
      this.maze.get(0).get(0).changeColor(Color.GREEN);
      this.maze.get(this.width - 1).get(this.length - 1).changeColor(Color.RED);
    }
    // EFFECT: starts depth-first search on the maze
    // can switch modes mid-search or after the maze has been solved
    else if (key.equals("d")) {
      this.reset();
      this.mode = new TextImage("currently using depth-first search", 15, Color.BLACK);
      this.scene = new WorldScene(220 + 20 * this.length, 220 + 20 * this.width);
      this.searching = true;
      this.finished = false;
      this.worklist = new Stack<Vertex>();
      this.worklist.add(this.maze.get(0).get(0));
      this.cameFromEdge.put(this.maze.get(0).get(0),
          new Edge(2 * this.maze.get(0).get(0).hashCode(), this.maze.get(0).get(0),
              this.maze.get(0).get(0)));
      for (ArrayList<Vertex> row : this.maze) {
        for (Vertex v : row) {
          v.changeColor(Color.GRAY);
        }
      }
      this.maze.get(0).get(0).changeColor(Color.GREEN);
      this.maze.get(this.width - 1).get(this.length - 1).changeColor(Color.RED);
    }
    // EFFECT: pauses and unpauses a search if one is in progress
    else if (key.equals("p")) {
      if (this.searching) {
        if (!this.paused) {
          this.paused = true;
          this.pause = new TextImage("the search is paused", 15, Color.BLACK);
        }
        else {
          this.paused = false;
          this.pause = new TextImage("", 15, Color.BLACK);
        }
      }
    }
  }

  // EFFECT: reconstructs the correct path through the maze from the cameFromEdge
  // HashMap and adds it to the ArrayList<Vertex> path
  void reconstruct(Vertex from, Vertex to) {
    this.path.add(to);
    this.path2.add(to);
    Vertex next = this.cameFromEdge.get(to).getFrom();
    while (!this.path.contains(from)) {
      this.path.add(0, next);
      this.path2.add(0, next);
      next = this.cameFromEdge.get(next).getFrom();
    }
  }
}

// examples and tests for mazes
class ExamplesMaze {
  EdgesByWeight ebw;
  Vertex v1;
  Vertex v2;
  Vertex v3;
  Vertex v4;
  Vertex v5;
  Vertex v6;
  Vertex v7;
  Vertex v8;
  Vertex v9;
  Vertex v99;
  Edge e1;
  Edge e2;
  Edge e3;
  Edge e4;
  Edge e5;
  Edge e6;
  Edge e7;
  Edge e8;
  Edge e9;
  Edge e10;
  Edge e11;
  Edge e12;
  MazeWorld mw;
  MazeWorld mw1;
  MazeWorld mw2;
  MazeWorld mwRect;
  ArrayList<Edge> mwKruskal;
  Stack<Integer> intStack;
  Queue<Integer> intQueue;
  LinkedList<Integer> intStackList;
  LinkedList<Integer> intQueueList;
  Stack<Vertex> vertexStack;
  Queue<Vertex> vertexQueue;
  LinkedList<Vertex> vertexStackList;
  LinkedList<Vertex> vertexQueueList;
  WorldScene scene;
  WorldScene scene2;
  WorldScene scene3;

  // initialize example data
  void initData() {
    this.ebw = new EdgesByWeight();

    // v1-v9 represent the vertices of a 3x3 grid
    this.v1 = new Vertex(0, 0, Color.GREEN);
    this.v2 = new Vertex(1, 0);
    this.v3 = new Vertex(2, 0);
    this.v4 = new Vertex(0, 1);
    this.v5 = new Vertex(1, 1);
    this.v6 = new Vertex(2, 1);
    this.v7 = new Vertex(0, 2);
    this.v8 = new Vertex(1, 2);
    this.v9 = new Vertex(2, 2, Color.RED);

    this.v99 = new Vertex(99, 99);

    // e1-e12 represent the edges in mw
    this.e1 = new Edge(11, this.v1, this.v2);
    this.e2 = new Edge(1, this.v1, this.v4);
    this.e3 = new Edge(5, this.v2, this.v3);
    this.e4 = new Edge(7, this.v2, this.v5);
    this.e5 = new Edge(4, this.v3, this.v6);
    this.e6 = new Edge(0, this.v4, this.v5);
    this.e7 = new Edge(9, this.v4, this.v7);
    this.e8 = new Edge(3, this.v5, this.v6);
    this.e9 = new Edge(6, this.v5, this.v8);
    this.e10 = new Edge(2, this.v6, this.v9);
    this.e11 = new Edge(10, this.v7, this.v8);
    this.e12 = new Edge(8, this.v8, this.v9);

    // example MazeWorld with 3x3 grid (seeded for testing)
    this.mw = new MazeWorld(3, 3, new Random(5));

    // example MazeWorld with 1x1 grid (seeded for testing)
    this.mw1 = new MazeWorld(1, 1, new Random(6));

    // example MazeWorld with 2x2 grid (seeded for testing)
    this.mw2 = new MazeWorld(2, 2, new Random(7));

    // example MazeWorld with 8x16 grid
    this.mwRect = new MazeWorld(8, 16);

    // ArrayList of edges in the mw MST
    this.mwKruskal = new ArrayList<Edge>(
        Arrays.asList(this.e6, this.e2, this.e10, this.e8, this.e5, this.e3, this.e9, this.e7));

    // Stack and Queue of Integers
    this.intStack = new Stack<Integer>();
    this.intQueue = new Queue<Integer>();

    // contents of a Stack and Queue of Integers
    this.intStackList = new LinkedList<Integer>(Arrays.asList(1, 2, 3));
    this.intQueueList = new LinkedList<Integer>(Arrays.asList(3, 2, 1));

    // Stack and Queue of vertices
    this.vertexStack = new Stack<Vertex>();
    this.vertexQueue = new Queue<Vertex>();

    // contents of a Stack and Queue of vertices
    this.vertexStackList = new LinkedList<Vertex>(Arrays.asList(this.v1, this.v2, this.v3));
    this.vertexQueueList = new LinkedList<Vertex>(Arrays.asList(this.v3, this.v2, this.v1));

    this.scene = new WorldScene(280, 280);
    this.scene2 = new WorldScene(280, 280);
    this.scene3 = new WorldScene(240, 240);
  }

  // test bigBang
  void testBigBang(Tester t) {
    this.initData();
    this.mw.bigBang(280, 280, 0.05);
    this.mw1.bigBang(240, 240, 0.05);
    this.mw2.bigBang(260, 260, 0.05);
    MazeWorld ex = new MazeWorld(50, 60, new Random(9));
    ex.bigBang(1220, 1420, 0.05);
  }

  // test EdgesByWeight Comparator compare(Edge, Edge) method
  void testEdgesByWeightComparator(Tester t) {
    this.initData();
    t.checkExpect(this.ebw.compare(this.e1, this.e3), 6);
  }

  // test ArrayList sort using the EdgesByWeight comparator
  // ArrayLists of Edges are always made up of unique weights
  void testEdgesByWeightSort(Tester t) {
    this.initData();

    ArrayList<Edge> toSort1 = new ArrayList<Edge>(Arrays.asList(new Edge(23, this.v1, this.v2),
        new Edge(19, this.v3, this.v6), new Edge(16, this.v9, this.v5)));
    ArrayList<Edge> toSort2 = new ArrayList<Edge>(Arrays.asList(new Edge(16, this.v1, this.v2),
        new Edge(23, this.v3, this.v6), new Edge(19, this.v9, this.v5)));
    ArrayList<Edge> sorted1 = new ArrayList<Edge>(Arrays.asList(new Edge(16, this.v9, this.v5),
        new Edge(19, this.v3, this.v6), new Edge(23, this.v1, this.v2)));
    ArrayList<Edge> sorted2 = new ArrayList<Edge>(Arrays.asList(new Edge(16, this.v1, this.v2),
        new Edge(19, this.v9, this.v5), new Edge(23, this.v3, this.v6)));

    toSort1.sort(this.ebw);
    toSort2.sort(this.ebw);

    t.checkExpect(toSort1, sorted1);
    t.checkExpect(toSort2, sorted2);
  }

  // test Vertex hashCode() method
  void testVertexHashCode(Tester t) {
    this.initData();
    t.checkExpect(this.v1.hashCode(), 0);
    t.checkExpect(this.v99.hashCode(), 990099);
  }

  // test Vertex equals(Object) method
  void testVertexEquals(Tester t) {
    this.initData();
    Vertex alsoV1 = new Vertex(0, 0);
    t.checkExpect(this.mw.maze.get(0).get(0).equals(this.mw.maze.get(0).get(0)), true);
    t.checkExpect(this.mw.maze.get(0).get(0).equals(this.v1), true);
    t.checkExpect(this.mw.maze.get(0).get(0).equals(alsoV1), true);
    t.checkExpect(this.mw.maze.get(0).get(0).equals(this.mw.maze.get(0).get(2)), false);
    t.checkExpect(this.v1.equals(this.mw.maze.get(0).get(0)), true);
    t.checkExpect(this.v1.equals(this.v1), true);
    t.checkExpect(alsoV1.equals(alsoV1), true);
    t.checkExpect(this.v1.equals(alsoV1), true);
    t.checkExpect(alsoV1.equals(this.v1), true);
    t.checkExpect(this.v2.equals(new Vertex(1, 0)), true);
    t.checkExpect(this.v2.equals(this.v1), false);
    t.checkExpect(this.v2.equals(this.e1), false);
  }

  // test Vertex drawVertex(WorldScene) method
  void testDrawVertex(Tester t) {
    this.initData();

    t.checkExpect(this.scene, this.scene2);

    this.v1.drawVertex(this.scene);
    this.scene2.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN), 120, 120);

    t.checkExpect(this.scene, this.scene2);

    this.v2.drawVertex(this.scene);
    this.scene2.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY), 140, 120);

    t.checkExpect(this.scene, this.scene2);
  }

  // test Vertex drawWallBetween(Vertex, WorldScene) method
  void testDrawWallBetween(Tester t) {
    this.initData();

    t.checkExpect(this.scene, this.scene2);

    this.v1.drawWallBetween(this.v2, this.scene);
    this.scene2.placeImageXY(new RectangleImage(2, 20, OutlineMode.SOLID, Color.BLACK), 130, 120);

    t.checkExpect(this.scene, this.scene2);

    this.v1.drawWallBetween(this.v4, this.scene);
    this.scene2.placeImageXY(new RectangleImage(20, 2, OutlineMode.SOLID, Color.BLACK), 120, 130);

    t.checkExpect(this.scene, this.scene2);

    this.v3.drawWallBetween(this.v6, this.scene);
    this.scene2.placeImageXY(new RectangleImage(20, 2, OutlineMode.SOLID, Color.BLACK), 160, 130);

    t.checkExpect(this.scene, this.scene2);

    this.v5.drawWallBetween(this.v9, this.scene);
    this.scene2.placeImageXY(new RectangleImage(2, 20, OutlineMode.SOLID, Color.BLACK), 150, 140);

    t.checkExpect(this.scene, this.scene2);
  }

  // test Vertex changeColor(Color) method
  void testChangeColor(Tester t) {
    this.initData();

    t.checkExpect(this.v1.color, Color.GREEN);

    this.v1.changeColor(Color.BLUE);

    t.checkExpect(this.v1.color, Color.BLUE);

    t.checkExpect(this.v2.color, Color.GRAY);

    this.v2.changeColor(Color.CYAN);

    t.checkExpect(this.v2.color, Color.CYAN);
  }

  // test Edge hashCode() method
  void testEdgeHashCode(Tester t) {
    this.initData();
    t.checkExpect(this.mw.edges.get(0).hashCode(), 1100010000);
    t.checkExpect(this.mw.edges.get(2).hashCode(), 500030000);
    t.checkExpect(this.e1.hashCode(), 1100010000);
    t.checkExpect(this.e3.hashCode(), 500030000);
  }

  // test Edge equals(Object) method
  void testEdgeEquals(Tester t) {
    this.initData();
    Edge alsoE1 = new Edge(11, new Vertex(0, 0), new Vertex(1, 0));
    t.checkExpect(this.mw.edges.get(0).equals(this.mw.edges.get(0)), true);
    t.checkExpect(this.mw.edges.get(0).equals(this.e1), true);
    t.checkExpect(this.mw.edges.get(0).equals(alsoE1), true);
    t.checkExpect(this.e1.equals(alsoE1), true);
    t.checkExpect(alsoE1.equals(this.e1), true);
    t.checkExpect(this.e1.equals(this.e1), true);
    t.checkExpect(alsoE1.equals(alsoE1), true);
    t.checkExpect(this.e2.equals(new Edge(1, new Vertex(0, 0), new Vertex(0, 1))), true);
    t.checkExpect(this.e1.equals(this.e3), false);
    t.checkExpect(this.e1.equals(this.v2), false);
  }

  // test Edge getFrom() method
  void testGetFrom(Tester t) {
    this.initData();
    t.checkExpect(this.mw.edges.get(0).getFrom(), this.v1);
    t.checkExpect(this.mw.edges.get(2).getFrom(), this.v2);
    t.checkExpect(this.e1.getFrom(), this.v1);
    t.checkExpect(this.e3.getFrom(), this.v2);
  }

  // test Edge getTo() method
  void testGetTo(Tester t) {
    this.initData();
    t.checkExpect(this.mw.edges.get(0).getTo(), this.v2);
    t.checkExpect(this.mw.edges.get(2).getTo(), this.v3);
    t.checkExpect(this.e1.getTo(), this.v2);
    t.checkExpect(this.e3.getTo(), this.v3);
  }

  // test Stack and Queue isEmpty() method
  void testIsEmpty(Tester t) {
    this.initData();

    t.checkExpect(this.intStack.isEmpty(), true);
    t.checkExpect(this.intQueue.isEmpty(), true);
    this.intStack.contents = this.intStackList;
    this.intQueue.contents = this.intQueueList;
    t.checkExpect(this.intStack.isEmpty(), false);
    t.checkExpect(this.intQueue.isEmpty(), false);

    t.checkExpect(this.vertexStack.isEmpty(), true);
    t.checkExpect(this.vertexQueue.isEmpty(), true);
    this.vertexStack.contents = this.vertexStackList;
    this.vertexQueue.contents = this.vertexQueueList;
    t.checkExpect(this.vertexStack.isEmpty(), false);
    t.checkExpect(this.vertexQueue.isEmpty(), false);
  }

  // test Stack and Queue add(T item) method
  void testAdd(Tester t) {
    this.initData();

    this.intStack.add(3);
    this.intQueue.add(3);
    t.checkExpect(this.intStack.contents.equals(new LinkedList<Integer>(Arrays.asList(3))), true);
    t.checkExpect(this.intQueue.contents.equals(new LinkedList<Integer>(Arrays.asList(3))), true);
    t.checkExpect(this.intStack.contents.equals(this.intStackList), false);
    t.checkExpect(this.intQueue.contents.equals(this.intQueueList), false);

    this.intStack.add(2);
    this.intQueue.add(2);
    t.checkExpect(this.intStack.contents.equals(new LinkedList<Integer>(Arrays.asList(2, 3))),
        true);
    t.checkExpect(this.intQueue.contents.equals(new LinkedList<Integer>(Arrays.asList(3, 2))),
        true);
    t.checkExpect(this.intStack.contents.equals(this.intStackList), false);
    t.checkExpect(this.intQueue.contents.equals(this.intQueueList), false);

    this.intStack.add(1);
    this.intQueue.add(1);
    t.checkExpect(this.intStack.contents.equals(this.intStackList), true);
    t.checkExpect(this.intQueue.contents.equals(this.intQueueList), true);

    this.vertexStack.add(this.v3);
    this.vertexQueue.add(this.v3);
    t.checkExpect(this.vertexStack.contents.equals(new LinkedList<Vertex>(Arrays.asList(this.v3))),
        true);
    t.checkExpect(this.vertexQueue.contents.equals(new LinkedList<Vertex>(Arrays.asList(this.v3))),
        true);
    t.checkExpect(this.vertexStack.contents.equals(this.vertexStackList), false);
    t.checkExpect(this.vertexQueue.contents.equals(this.vertexQueueList), false);

    this.vertexStack.add(this.v2);
    this.vertexQueue.add(this.v2);
    t.checkExpect(
        this.vertexStack.contents.equals(new LinkedList<Vertex>(Arrays.asList(this.v2, this.v3))),
        true);
    t.checkExpect(
        this.vertexQueue.contents.equals(new LinkedList<Vertex>(Arrays.asList(this.v3, this.v2))),
        true);
    t.checkExpect(this.vertexStack.contents.equals(this.vertexStackList), false);
    t.checkExpect(this.vertexQueue.contents.equals(this.vertexQueueList), false);

    this.vertexStack.add(this.v1);
    this.vertexQueue.add(this.v1);
    t.checkExpect(this.vertexStack.contents.equals(this.vertexStackList), true);
    t.checkExpect(this.vertexQueue.contents.equals(this.vertexQueueList), true);
  }

  // test Stack and Queue remove() method
  void testRemove(Tester t) {
    this.initData();

    this.intStack.contents = this.intStackList;
    this.intQueue.contents = this.intQueueList;
    t.checkExpect(intStack.remove(), 1);
    t.checkExpect(this.intStack.contents.equals(new LinkedList<Integer>(Arrays.asList(2, 3))),
        true);
    t.checkExpect(intStack.remove(), 2);
    t.checkExpect(this.intStack.contents.equals(new LinkedList<Integer>(Arrays.asList(3))), true);
    t.checkExpect(intStack.remove(), 3);
    t.checkExpect(this.intStack.contents.equals(new LinkedList<Integer>(Arrays.asList())), true);
    t.checkExpect(intQueue.remove(), 3);
    t.checkExpect(this.intQueue.contents.equals(new LinkedList<Integer>(Arrays.asList(2, 1))),
        true);
    t.checkExpect(intQueue.remove(), 2);
    t.checkExpect(this.intQueue.contents.equals(new LinkedList<Integer>(Arrays.asList(1))), true);
    t.checkExpect(intQueue.remove(), 1);
    t.checkExpect(this.intQueue.contents.equals(new LinkedList<Integer>(Arrays.asList())), true);

    this.vertexStack.contents = this.vertexStackList;
    this.vertexQueue.contents = this.vertexQueueList;
    t.checkExpect(vertexStack.remove(), this.v1);
    t.checkExpect(
        this.vertexStack.contents.equals(new LinkedList<Vertex>(Arrays.asList(this.v2, this.v3))),
        true);
    t.checkExpect(vertexStack.remove(), this.v2);
    t.checkExpect(this.vertexStack.contents.equals(new LinkedList<Vertex>(Arrays.asList(this.v3))),
        true);
    t.checkExpect(vertexStack.remove(), this.v3);
    t.checkExpect(this.vertexStack.contents.equals(new LinkedList<Vertex>(Arrays.asList())), true);
    t.checkExpect(vertexQueue.remove(), this.v3);
    t.checkExpect(
        this.vertexQueue.contents.equals(new LinkedList<Vertex>(Arrays.asList(this.v2, this.v1))),
        true);
    t.checkExpect(vertexQueue.remove(), this.v2);
    t.checkExpect(this.vertexQueue.contents.equals(new LinkedList<Vertex>(Arrays.asList(this.v1))),
        true);
    t.checkExpect(vertexQueue.remove(), this.v1);
    t.checkExpect(this.vertexQueue.contents.equals(new LinkedList<Vertex>(Arrays.asList())), true);
  }

  // test MazeWorld generateMaze() method
  void testGenerateMaze(Tester t) {
    this.initData();

    // allows for testing of just generateMaze() without the effects of kruskal(),
    // which also gets called in the MazeWorld constructors
    this.mw.maze.clear();
    this.mw.representatives.clear();
    this.mw.generateMaze();

    t.checkExpect(this.mw.maze.size(), 3);
    t.checkExpect(this.mw.maze.get(0).size(), 3);
    t.checkExpect(this.mw.maze.get(1).size(), 3);
    t.checkExpect(this.mw.maze.get(2).size(), 3);
    t.checkExpect(this.mw.representatives.size(), 9);

    t.checkExpect(this.mw.maze.get(0).get(0), this.v1);
    t.checkExpect(this.mw.maze.get(0).get(1), this.v2);
    t.checkExpect(this.mw.maze.get(0).get(2), this.v3);
    t.checkExpect(this.mw.maze.get(1).get(0), this.v4);
    t.checkExpect(this.mw.maze.get(1).get(1), this.v5);
    t.checkExpect(this.mw.maze.get(1).get(2), this.v6);
    t.checkExpect(this.mw.maze.get(2).get(0), this.v7);
    t.checkExpect(this.mw.maze.get(2).get(1), this.v8);
    t.checkExpect(this.mw.maze.get(2).get(2), this.v9);

    t.checkExpect(this.mw.representatives.get(this.mw.maze.get(0).get(0)), this.v1);
    t.checkExpect(this.mw.representatives.get(this.mw.maze.get(0).get(1)), this.v2);
    t.checkExpect(this.mw.representatives.get(this.mw.maze.get(0).get(2)), this.v3);
    t.checkExpect(this.mw.representatives.get(this.mw.maze.get(1).get(0)), this.v4);
    t.checkExpect(this.mw.representatives.get(this.mw.maze.get(1).get(1)), this.v5);
    t.checkExpect(this.mw.representatives.get(this.mw.maze.get(1).get(2)), this.v6);
    t.checkExpect(this.mw.representatives.get(this.mw.maze.get(2).get(0)), this.v7);
    t.checkExpect(this.mw.representatives.get(this.mw.maze.get(2).get(1)), this.v8);
    t.checkExpect(this.mw.representatives.get(this.mw.maze.get(2).get(2)), this.v9);

    this.mw1.maze.clear();
    this.mw1.representatives.clear();
    this.mw1.generateMaze();

    t.checkExpect(this.mw1.maze.size(), 1);
    t.checkExpect(this.mw1.maze.get(0).size(), 1);
    t.checkExpect(this.mw1.representatives.size(), 1);
    t.checkExpect(this.mw1.maze.get(0).get(0), this.v1);
    t.checkExpect(this.mw1.representatives.get(this.mw.maze.get(0).get(0)), this.v1);
  }

  // test MazeWorld assignEdges() method
  void testAssignEdges(Tester t) {
    this.initData();
    ArrayList<Integer> expectedWeights = new ArrayList<Integer>(
        Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
    ArrayList<Integer> actualWeights = new ArrayList<Integer>();
    t.checkExpect(this.mw1.edges.size(), 0);
    t.checkExpect(this.mw.edges.size(), 12);
    t.checkExpect(this.mw.edges.size(), expectedWeights.size());
    for (Edge e : this.mw.edges) {
      t.checkExpect(expectedWeights.contains(e.weight), true);
      actualWeights.add(e.weight);
    }
    for (Integer i : expectedWeights) {
      t.checkExpect(actualWeights.contains(i), true);
    }

    t.checkExpect(this.mw.edges.get(0), this.e1);
    t.checkExpect(this.mw.edges.get(1), this.e2);
    t.checkExpect(this.mw.edges.get(2), this.e3);
    t.checkExpect(this.mw.edges.get(3), this.e4);
    t.checkExpect(this.mw.edges.get(4), this.e5);
    t.checkExpect(this.mw.edges.get(5), this.e6);
    t.checkExpect(this.mw.edges.get(6), this.e7);
    t.checkExpect(this.mw.edges.get(7), this.e8);
    t.checkExpect(this.mw.edges.get(8), this.e9);
    t.checkExpect(this.mw.edges.get(9), this.e10);
    t.checkExpect(this.mw.edges.get(10), this.e11);
    t.checkExpect(this.mw.edges.get(11), this.e12);
  }

  // test MazeWorld assignEdgesBias(boolean) method
  void testAssignEdgeBias(Tester t) {
    this.initData();

    // allows for testing of just assignEdgesBias(boolean)
    // without all the effects of onKeyEvent(String), which is the only place it's
    // called
    this.mw.edges = new ArrayList<Edge>();
    this.mw.assignEdgesBias(false);

    this.mw1.edges = new ArrayList<Edge>();
    this.mw1.assignEdgesBias(false);

    ArrayList<Integer> expectedWeights = new ArrayList<Integer>(
        Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
    ArrayList<Integer> actualWeights = new ArrayList<Integer>();
    t.checkExpect(this.mw1.edges.size(), 0);
    t.checkExpect(this.mw.edges.size(), 12);
    t.checkExpect(this.mw.edges.size(), expectedWeights.size());
    for (Edge e : this.mw.edges) {
      t.checkExpect(expectedWeights.contains(e.weight), true);
      actualWeights.add(e.weight);
    }
    for (Integer i : expectedWeights) {
      t.checkExpect(actualWeights.contains(i), true);
    }

    Edge edge1 = new Edge(7, this.v1, this.v2);
    Edge edge2 = new Edge(0, this.v1, this.v4);
    Edge edge3 = new Edge(10, this.v2, this.v3);
    Edge edge4 = new Edge(1, this.v2, this.v5);
    Edge edge5 = new Edge(2, this.v3, this.v6);
    Edge edge6 = new Edge(8, this.v4, this.v5);
    Edge edge7 = new Edge(5, this.v4, this.v7);
    Edge edge8 = new Edge(6, this.v5, this.v6);
    Edge edge9 = new Edge(4, this.v5, this.v8);
    Edge edge10 = new Edge(3, this.v6, this.v9);
    Edge edge11 = new Edge(9, this.v7, this.v8);
    Edge edge12 = new Edge(11, this.v8, this.v9);

    ArrayList<Edge> horizontalEdges = new ArrayList<Edge>(
        Arrays.asList(edge1, edge3, edge6, edge8, edge11, edge12));
    ArrayList<Edge> verticalEdges = new ArrayList<Edge>(
        Arrays.asList(edge2, edge4, edge5, edge7, edge9, edge10));
    ArrayList<Integer> lowerWeights = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4, 5));
    ArrayList<Integer> upperWeights = new ArrayList<Integer>(Arrays.asList(6, 7, 8, 9, 10, 11));

    t.checkExpect(this.mw.edges.get(0), edge1);
    t.checkExpect(this.mw.edges.get(1), edge2);
    t.checkExpect(this.mw.edges.get(2), edge3);
    t.checkExpect(this.mw.edges.get(3), edge4);
    t.checkExpect(this.mw.edges.get(4), edge5);
    t.checkExpect(this.mw.edges.get(5), edge6);
    t.checkExpect(this.mw.edges.get(6), edge7);
    t.checkExpect(this.mw.edges.get(7), edge8);
    t.checkExpect(this.mw.edges.get(8), edge9);
    t.checkExpect(this.mw.edges.get(9), edge10);
    t.checkExpect(this.mw.edges.get(10), edge11);
    t.checkExpect(this.mw.edges.get(11), edge12);

    for (Edge e : horizontalEdges) {
      t.checkExpect(upperWeights.contains(e.weight), true);
    }
    for (Edge e : verticalEdges) {
      t.checkExpect(lowerWeights.contains(e.weight), true);
    }

    this.initData();

    // allows for testing of just assignEdgesBias(boolean)
    // without all the effects of onKeyEvent(String), which is the only place it's
    // called
    this.mw.edges = new ArrayList<Edge>();
    this.mw.assignEdgesBias(true);

    this.mw1.edges = new ArrayList<Edge>();
    this.mw1.assignEdgesBias(true);

    ArrayList<Integer> actualWeights2 = new ArrayList<Integer>();
    t.checkExpect(this.mw1.edges.size(), 0);
    t.checkExpect(this.mw.edges.size(), 12);
    t.checkExpect(this.mw.edges.size(), expectedWeights.size());
    for (Edge e : this.mw.edges) {
      t.checkExpect(expectedWeights.contains(e.weight), true);
      actualWeights2.add(e.weight);
    }
    for (Integer i : expectedWeights) {
      t.checkExpect(actualWeights2.contains(i), true);
    }

    Edge eOne = new Edge(1, this.v1, this.v2);
    Edge eTwo = new Edge(6, this.v1, this.v4);
    Edge eThree = new Edge(4, this.v2, this.v3);
    Edge eFour = new Edge(7, this.v2, this.v5);
    Edge eFive = new Edge(8, this.v3, this.v6);
    Edge eSix = new Edge(2, this.v4, this.v5);
    Edge eSeven = new Edge(11, this.v4, this.v7);
    Edge eEight = new Edge(0, this.v5, this.v6);
    Edge eNine = new Edge(10, this.v5, this.v8);
    Edge eTen = new Edge(9, this.v6, this.v9);
    Edge eEleven = new Edge(3, this.v7, this.v8);
    Edge eTwelve = new Edge(5, this.v8, this.v9);

    ArrayList<Edge> horizontalEdges2 = new ArrayList<Edge>(
        Arrays.asList(eOne, eThree, eSix, eEight, eEleven, eTwelve));
    ArrayList<Edge> verticalEdges2 = new ArrayList<Edge>(
        Arrays.asList(eTwo, eFour, eFive, eSeven, eNine, eTen));

    t.checkExpect(this.mw.edges.get(0), eOne);
    t.checkExpect(this.mw.edges.get(1), eTwo);
    t.checkExpect(this.mw.edges.get(2), eThree);
    t.checkExpect(this.mw.edges.get(3), eFour);
    t.checkExpect(this.mw.edges.get(4), eFive);
    t.checkExpect(this.mw.edges.get(5), eSix);
    t.checkExpect(this.mw.edges.get(6), eSeven);
    t.checkExpect(this.mw.edges.get(7), eEight);
    t.checkExpect(this.mw.edges.get(8), eNine);
    t.checkExpect(this.mw.edges.get(9), eTen);
    t.checkExpect(this.mw.edges.get(10), eEleven);
    t.checkExpect(this.mw.edges.get(11), eTwelve);

    for (Edge e : horizontalEdges2) {
      t.checkExpect(lowerWeights.contains(e.weight), true);
    }
    for (Edge e : verticalEdges2) {
      t.checkExpect(upperWeights.contains(e.weight), true);
    }

  }

  // test MazeWorld find(Vertex, ArrayList<Vertex>) method
  void testFind(Tester t) {
    this.initData();

    t.checkExpect(this.mw1.find(this.mw1.maze.get(0).get(0), new ArrayList<Vertex>()), this.v1);
    this.mw1.representatives.put(this.mw1.maze.get(0).get(0), this.mw1.maze.get(0).get(0));
    t.checkExpect(this.mw1.find(this.mw1.maze.get(0).get(0), new ArrayList<Vertex>()), this.v1);

    t.checkExpect(this.mw.find(this.mw.maze.get(0).get(0), new ArrayList<Vertex>()), this.v2);
    t.checkExpect(this.mw.find(this.mw.maze.get(1).get(0), new ArrayList<Vertex>()), this.v2);
    t.checkExpect(this.mw.find(this.mw.maze.get(2).get(0), new ArrayList<Vertex>()), this.v2);

    this.mw.representatives.put(this.mw.maze.get(0).get(0), this.mw.maze.get(0).get(0));
    this.mw.representatives.put(this.mw.maze.get(1).get(0), this.mw.maze.get(1).get(0));
    this.mw.representatives.put(this.mw.maze.get(2).get(0), this.mw.maze.get(2).get(0));

    t.checkExpect(this.mw.find(this.mw.maze.get(0).get(0), new ArrayList<Vertex>()), this.v1);
    t.checkExpect(this.mw.find(this.mw.maze.get(1).get(0), new ArrayList<Vertex>()), this.v4);
    t.checkExpect(this.mw.find(this.mw.maze.get(2).get(0), new ArrayList<Vertex>()), this.v7);

    this.mw.representatives.put(this.mw.maze.get(0).get(0), this.mw.maze.get(1).get(0));

    t.checkExpect(this.mw.find(this.mw.maze.get(0).get(0), new ArrayList<Vertex>()), this.v4);

    this.mw.representatives.put(this.mw.maze.get(1).get(0), this.mw.maze.get(2).get(0));

    t.checkExpect(this.mw.find(this.mw.maze.get(0).get(0), new ArrayList<Vertex>()), this.v7);
    t.checkExpect(this.mw.find(this.mw.maze.get(1).get(0), new ArrayList<Vertex>()), this.v7);
  }

  // test MazeWorld union(Vertex, Vertex) method
  void testUnion(Tester t) {
    this.initData();

    this.mw1.union(this.mw1.maze.get(0).get(0), this.mw1.maze.get(0).get(0));
    t.checkExpect(this.mw1.find(this.mw1.maze.get(0).get(0), new ArrayList<Vertex>()), this.v1);

    this.mw.representatives.put(this.mw.maze.get(1).get(2), this.mw.maze.get(1).get(2));
    this.mw.representatives.put(this.mw.maze.get(2).get(2), this.mw.maze.get(2).get(2));

    t.checkExpect(this.mw.find(this.mw.maze.get(1).get(2), new ArrayList<Vertex>()), this.v6);
    t.checkExpect(this.mw.find(this.mw.maze.get(2).get(2), new ArrayList<Vertex>()), this.v9);

    this.mw.union(this.mw.maze.get(2).get(2), this.mw.maze.get(1).get(2));

    t.checkExpect(this.mw.find(this.mw.maze.get(1).get(2), new ArrayList<Vertex>()), this.v9);
    t.checkExpect(this.mw.find(this.mw.maze.get(2).get(2), new ArrayList<Vertex>()), this.v9);

    this.mw.representatives.put(this.mw.maze.get(0).get(2), this.mw.maze.get(1).get(2));

    t.checkExpect(this.mw.find(this.mw.maze.get(0).get(2), new ArrayList<Vertex>()), this.v9);

    this.mw.union(this.mw.maze.get(0).get(2), this.mw.maze.get(0).get(1));

    t.checkExpect(this.mw.find(this.mw.maze.get(0).get(1), new ArrayList<Vertex>()), this.v9);
    t.checkExpect(this.mw.find(this.mw.maze.get(0).get(2), new ArrayList<Vertex>()), this.v9);
  }

  // test MazeWorld kruskal() method
  void testKruskal(Tester t) {
    this.initData();

    t.checkExpect(this.mw1.mst, new ArrayList<Edge>());

    t.checkExpect(this.mw.mst, this.mwKruskal);

    Vertex onlyRep = this.mw.find(this.mw.maze.get(0).get(0), new ArrayList<Vertex>());

    for (ArrayList<Vertex> a : this.mw.maze) {
      for (Vertex v : a) {
        t.checkExpect(this.mw.find(v, new ArrayList<Vertex>()), onlyRep);
      }
    }
  }

  // test the MazeWorld getTime() method
  void testGetTime(Tester t) {
    this.initData();

    this.mw.time = 1;
    t.checkExpect(this.mw.getTime(), "Time Elapsed: 0");

    this.mw.time = 20;
    t.checkExpect(this.mw.getTime(), "Time Elapsed: 1");

    this.mw.time = 40;
    t.checkExpect(this.mw.getTime(), "Time Elapsed: 2");

    this.mw.onKeyEvent("r");

    t.checkExpect(this.mw.getTime(), "Time Elapsed: 0");

    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();

    t.checkExpect(this.mw.getTime(), "Time Elapsed: 0");

    this.mw.onKeyEvent("n");

    t.checkExpect(this.mw.getTime(), "Time Elapsed: 0");

    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();

    t.checkExpect(this.mw.getTime(), "Time Elapsed: 0");

    this.mw.onKeyEvent("h");

    t.checkExpect(this.mw.getTime(), "Time Elapsed: 0");

    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();

    t.checkExpect(this.mw.getTime(), "Time Elapsed: 0");

    this.mw.onKeyEvent("v");

    t.checkExpect(this.mw.getTime(), "Time Elapsed: 0");

    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();

    t.checkExpect(this.mw.getTime(), "Time Elapsed: 0");

    this.mw.onKeyEvent("b");

    t.checkExpect(this.mw.getTime(), "Time Elapsed: 0");

    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();

    t.checkExpect(this.mw.getTime(), "Total Time Taken: 0");

    this.mw.onKeyEvent("d");

    t.checkExpect(this.mw.getTime(), "Time Elapsed: 0");

    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();

    t.checkExpect(this.mw.getTime(), "Total Time Taken: 0");

    this.mwRect.onKeyEvent("b");

    t.checkExpect(this.mwRect.getTime(), "Time Elapsed: 0");

    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();

    t.checkExpect(this.mwRect.getTime(), "Time Elapsed: 1");

    this.mwRect.onKeyEvent("d");

    t.checkExpect(this.mwRect.getTime(), "Time Elapsed: 0");

    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();

    t.checkExpect(this.mwRect.getTime(), "Time Elapsed: 1");
  }

  // test MazeWorld reset() method
  void testReset(Tester t) {
    this.initData();

    this.mw.onKeyEvent("b");
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();

    LinkedList<Vertex> as = new LinkedList<Vertex>();
    as.add(this.v6);
    as.add(this.v7);
    as.add(this.v5);
    as.add(this.v4);
    as.add(this.v1);
    LinkedList<Vertex> as2 = as;

    t.checkExpect(this.mw.time, 5);
    t.checkExpect(this.mw.alreadySeen, as);
    t.checkExpect(this.mw.alreadySeen2, as2);
    t.checkExpect(this.mw.path, new ArrayList<Vertex>());
    t.checkExpect(this.mw.path2, new ArrayList<Vertex>());

    this.mw.reset();

    t.checkExpect(this.mw.time, 0);
    t.checkExpect(this.mw.alreadySeen, new LinkedList<Vertex>());
    t.checkExpect(this.mw.alreadySeen2, new LinkedList<Vertex>());
    t.checkExpect(this.mw.path, new ArrayList<Vertex>());
    t.checkExpect(this.mw.path2, new ArrayList<Vertex>());
  }

  // test MazeWorld newMaze() method
  void testNewMaze(Tester t) {
    this.initData();

    ArrayList<Vertex> row1 = new ArrayList<Vertex>(Arrays.asList(this.v1, this.v2, this.v3));
    ArrayList<Vertex> row2 = new ArrayList<Vertex>(Arrays.asList(this.v4, this.v5, this.v6));
    ArrayList<Vertex> row3 = new ArrayList<Vertex>(Arrays.asList(this.v7, this.v8, this.v9));

    t.checkExpect(this.mw.maze, new ArrayList<ArrayList<Vertex>>(Arrays.asList(row1, row2, row3)));
    t.checkExpect(this.mw1.maze, new ArrayList<ArrayList<Vertex>>(
        Arrays.asList(new ArrayList<Vertex>(Arrays.asList(this.v1)))));
    t.checkExpect(this.mw.edges, new ArrayList<Edge>(Arrays.asList(this.e1, this.e2, this.e3,
        this.e4, this.e5, this.e6, this.e7, this.e8, this.e9, this.e10, this.e11, this.e12)));
    t.checkExpect(this.mw1.edges, new ArrayList<Edge>());
    t.checkExpect(this.mw1.mst, new ArrayList<Edge>());
    t.checkExpect(this.mw.mst, this.mwKruskal);

    t.checkExpect(this.mw.mode, new TextImage("", 15, Color.BLACK));
    t.checkExpect(this.mw1.mode, new TextImage("", 15, Color.BLACK));
    t.checkExpect(this.mw.searching, false);
    t.checkExpect(this.mw.finished, false);
    t.checkExpect(this.mw1.searching, false);
    t.checkExpect(this.mw1.finished, false);

    this.mw.newMaze();

    t.checkExpect(this.mw.maze, new ArrayList<ArrayList<Vertex>>(Arrays.asList(row1, row2, row3)));
    t.checkExpect(this.mw1.maze, new ArrayList<ArrayList<Vertex>>(
        Arrays.asList(new ArrayList<Vertex>(Arrays.asList(this.v1)))));
    t.checkFail(this.mw.edges, new ArrayList<Edge>(Arrays.asList(this.e1, this.e2, this.e3, this.e4,
        this.e5, this.e6, this.e7, this.e8, this.e9, this.e10, this.e11, this.e12)));
    t.checkExpect(this.mw1.edges, new ArrayList<Edge>());
    t.checkExpect(this.mw1.mst, new ArrayList<Edge>());
    t.checkExpect(this.mw.mst, this.mwKruskal);
    t.checkExpect(this.mw.mode, new TextImage("", 15, Color.BLACK));
    t.checkExpect(this.mw1.mode, new TextImage("", 15, Color.BLACK));
    t.checkExpect(this.mw.searching, false);
    t.checkExpect(this.mw.finished, false);
    t.checkExpect(this.mw1.searching, false);
    t.checkExpect(this.mw1.finished, false);
  }

  // test MazeWorld makeScene() method
  void testMakeScene(Tester t) {
    this.initData();

    TextImage title = new TextImage("Prepare to Be A-mazed", 20, FontStyle.BOLD, Color.BLACK);
    TextImage instructions1 = new TextImage("Press B to search the maze with breadth-first search",
        10, Color.BLACK);
    TextImage instructions2 = new TextImage("and D to search the maze with depth-first search.", 10,
        Color.BLACK);
    TextImage resetInfo = new TextImage("Press R to reset the game, N to create a new maze,", 10,
        Color.BLACK);
    TextImage resetInfo2 = new TextImage("H to create a new horizontally-biased maze,", 10,
        Color.BLACK);
    TextImage resetInfo3 = new TextImage("or V to create a new vertically-biased maze.", 10,
        Color.BLACK);
    TextImage timer = new TextImage("Time Elapsed: " + 0, 15, Color.BLACK);
    TextImage mode = new TextImage("", 15, Color.BLACK);
    TextImage pause = new TextImage("", 15, Color.BLACK);
    TextImage soFar = new TextImage("Vertices searched so far: " + 0, 15, Color.BLACK);

    this.scene.placeImageXY(title, 140, 12);
    this.scene.placeImageXY(instructions1, 140, 30);
    this.scene.placeImageXY(instructions2, 140, 40);
    this.scene.placeImageXY(resetInfo, 140, 250);
    this.scene.placeImageXY(resetInfo2, 140, 260);
    this.scene.placeImageXY(resetInfo3, 140, 270);
    this.scene.placeImageXY(timer, 140, 230);
    this.scene.placeImageXY(mode, 140, 60);
    this.scene.placeImageXY(pause, 130, 80);
    this.scene.placeImageXY(soFar, 140, 180);

    this.scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN), 120, 120);
    this.scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY), 140, 120);
    this.scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY), 160, 120);
    this.scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY), 120, 140);
    this.scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY), 140, 140);
    this.scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY), 160, 140);
    this.scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY), 120, 160);
    this.scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY), 140, 160);
    this.scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.RED), 160, 160);
    this.scene.placeImageXY(new RectangleImage(2, 20, OutlineMode.SOLID, Color.BLACK), 130, 120);
    this.scene.placeImageXY(new RectangleImage(20, 2, OutlineMode.SOLID, Color.BLACK), 140, 130);
    this.scene.placeImageXY(new RectangleImage(2, 20, OutlineMode.SOLID, Color.BLACK), 130, 160);
    this.scene.placeImageXY(new RectangleImage(2, 20, OutlineMode.SOLID, Color.BLACK), 150, 160);

    t.checkExpect(this.mw.makeScene(), this.scene);

    this.initData();

    this.mw.onKeyEvent("b");
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.finished = true;

    TextImage timer2 = new TextImage("Total Time Taken: " + 0, 15, Color.BLACK);
    TextImage mode1 = new TextImage("currently using breadth-first search", 15, Color.BLACK);
    TextImage soFar1 = new TextImage("Total Vertices Searched: " + 7, 15, Color.BLACK);
    TextImage stats = new TextImage("The correct path is " + 5 + " vertices long", 15, Color.BLACK);
    TextImage stats2 = new TextImage(2 + " wrong steps were taken", 15, Color.BLACK);

    this.scene.placeImageXY(title, 140, 12);
    this.scene.placeImageXY(instructions1, 140, 30);
    this.scene.placeImageXY(instructions2, 140, 40);
    this.scene.placeImageXY(resetInfo, 140, 250);
    this.scene.placeImageXY(resetInfo2, 140, 260);
    this.scene.placeImageXY(resetInfo3, 140, 270);
    this.scene.placeImageXY(timer2, 140, 230);
    this.scene.placeImageXY(mode1, 140, 60);
    this.scene.placeImageXY(pause, 130, 80);
    this.scene.placeImageXY(soFar1, 140, 180);
    this.scene.placeImageXY(stats, 140, 195);
    this.scene.placeImageXY(stats2, 140, 210);

    this.scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN), 120, 120);
    this.scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY), 140, 120);
    this.scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY), 160, 120);
    this.scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY), 120, 140);
    this.scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY), 140, 140);
    this.scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY), 160, 140);
    this.scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY), 120, 160);
    this.scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY), 140, 160);
    this.scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.RED), 160, 160);
    this.scene.placeImageXY(new RectangleImage(2, 20, OutlineMode.SOLID, Color.BLACK), 130, 120);
    this.scene.placeImageXY(new RectangleImage(20, 2, OutlineMode.SOLID, Color.BLACK), 140, 130);
    this.scene.placeImageXY(new RectangleImage(2, 20, OutlineMode.SOLID, Color.BLACK), 130, 160);
    this.scene.placeImageXY(new RectangleImage(2, 20, OutlineMode.SOLID, Color.BLACK), 150, 160);

    t.checkExpect(this.mw.makeScene(), this.scene);
  }

  // test the MazeWorld onTick() method
  void testOnTick(Tester t) {
    this.initData();

    t.checkExpect(this.mwRect.getTime(), "Time Elapsed: 0");

    this.mwRect.onKeyEvent("b");
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();
    this.mwRect.onTick();

    t.checkExpect(this.mwRect.getTime(), "Time Elapsed: 1");

    this.initData();

    t.checkExpect(this.mw.searching, false);
    t.checkExpect(this.mw.finished, false);

    this.mw.onKeyEvent("b");
    this.mw.onTick();

    t.checkExpect(this.mw.searching, true);
    t.checkExpect(this.mw.finished, false);

    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();

    t.checkExpect(this.mw.searching, false);
    t.checkExpect(this.mw.finished, true);

    this.mw.onKeyEvent("d");
    this.mw.onTick();

    t.checkExpect(this.mw.searching, true);
    t.checkExpect(this.mw.finished, false);

    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();

    t.checkExpect(this.mw.searching, false);
    t.checkExpect(this.mw.finished, true);

    this.initData();

    Queue<Vertex> wl = new Queue<Vertex>();
    Stack<Vertex> wl2 = new Stack<Vertex>();

    this.mw.onKeyEvent("b");

    this.mw.onTick();
    wl.add(this.v4);
    t.checkExpect(this.mw.worklist, wl);

    this.mw.onTick();
    wl.remove();
    wl.add(this.v5);
    wl.add(this.v7);
    t.checkExpect(this.mw.worklist, wl);

    this.mw.onKeyEvent("d");

    this.mw.onTick();
    wl2.add(this.v4);
    t.checkExpect(this.mw.worklist, wl2);

    this.mw.onTick();
    wl2.remove();
    wl2.add(this.v5);
    wl2.add(this.v7);
    t.checkExpect(this.mw.worklist, wl2);

    this.initData();

    LinkedList<Vertex> bSeen = new LinkedList<Vertex>();
    LinkedList<Vertex> dSeen = new LinkedList<Vertex>();

    t.checkExpect(this.mw.alreadySeen, bSeen);

    this.mw.onKeyEvent("b");

    this.mw.onTick();
    bSeen.add(this.v1);
    t.checkExpect(this.mw.alreadySeen, bSeen);

    this.mw.onTick();
    bSeen.addFirst(this.v4);
    t.checkExpect(this.mw.alreadySeen, bSeen);

    this.mw.onTick();
    bSeen.addFirst(this.v5);
    t.checkExpect(this.mw.alreadySeen, bSeen);

    this.mw.onKeyEvent("d");

    t.checkExpect(this.mw.alreadySeen, dSeen);
    this.mw.onTick();
    dSeen.add(this.v1);
    t.checkExpect(this.mw.alreadySeen, dSeen);

    this.mw.onTick();
    dSeen.addFirst(this.v4);
    t.checkExpect(this.mw.alreadySeen, dSeen);

    this.mw.onTick();
    dSeen.addFirst(this.v7);
    t.checkExpect(this.mw.alreadySeen, dSeen);

    this.initData();

    LinkedList<Vertex> bSeen1 = new LinkedList<Vertex>();
    LinkedList<Vertex> dSeen1 = new LinkedList<Vertex>();

    t.checkExpect(this.mw.alreadySeen2, bSeen1);

    this.mw.onKeyEvent("b");

    this.mw.onTick();
    bSeen1.add(this.v1);
    t.checkExpect(this.mw.alreadySeen2, bSeen1);

    this.mw.onTick();
    bSeen1.addFirst(this.v4);
    t.checkExpect(this.mw.alreadySeen2, bSeen1);

    this.mw.onTick();
    bSeen1.addFirst(this.v5);
    t.checkExpect(this.mw.alreadySeen2, bSeen1);

    this.mw.onKeyEvent("d");

    t.checkExpect(this.mw.alreadySeen2, dSeen1);
    this.mw.onTick();
    dSeen1.add(this.v1);
    t.checkExpect(this.mw.alreadySeen2, dSeen1);

    this.mw.onTick();
    dSeen1.addFirst(this.v4);
    t.checkExpect(this.mw.alreadySeen2, dSeen1);

    this.mw.onTick();
    dSeen1.addFirst(this.v7);
    t.checkExpect(this.mw.alreadySeen2, dSeen1);

    this.initData();

    HashMap<Vertex, Edge> cfe = new HashMap<Vertex, Edge>();

    t.checkExpect(this.mw.cameFromEdge, cfe);

    this.mw.onKeyEvent("b");

    this.mw.onTick();
    cfe.put(this.v1, new Edge(0, this.v1, this.v1));
    cfe.put(this.v4, new Edge(1, this.v1, this.v4));
    t.checkExpect(this.mw.cameFromEdge, cfe);

    this.mw.onKeyEvent("d");

    this.mw.onTick();
    cfe.put(this.v1, new Edge(0, this.v1, this.v1));
    cfe.put(this.v4, new Edge(1, this.v1, this.v4));
    t.checkExpect(this.mw.cameFromEdge, cfe);
  }

  // test the MazeWorld onKeyEvent(String) method
  void testOnKeyEvent(Tester t) {
    this.initData();

    t.checkExpect(this.mw.length, 3);
    t.checkExpect(this.mw.width, 3);
    t.checkExpect(this.mw.edges.size(), 12);
    t.checkExpect(this.mw.mst.size(), 8);
    t.checkExpect(this.mw.maze.size(), 3);
    t.checkExpect(this.mw.maze.get(0).size(), 3);
    t.checkExpect(this.mw.maze.get(1).size(), 3);
    t.checkExpect(this.mw.maze.get(2).size(), 3);
    t.checkExpect(this.mw.representatives.size(), 9);

    t.checkExpect(this.mw.maze.get(0).get(0), this.v1);
    t.checkExpect(this.mw.maze.get(0).get(1), this.v2);
    t.checkExpect(this.mw.maze.get(0).get(2), this.v3);
    t.checkExpect(this.mw.maze.get(1).get(0), this.v4);
    t.checkExpect(this.mw.maze.get(1).get(1), this.v5);
    t.checkExpect(this.mw.maze.get(1).get(2), this.v6);
    t.checkExpect(this.mw.maze.get(2).get(0), this.v7);
    t.checkExpect(this.mw.maze.get(2).get(1), this.v8);
    t.checkExpect(this.mw.maze.get(2).get(2), this.v9);

    Vertex onlyRep = this.mw.find(this.mw.maze.get(0).get(0), new ArrayList<Vertex>());

    for (ArrayList<Vertex> a : this.mw.maze) {
      for (Vertex v : a) {
        t.checkExpect(this.mw.find(v, new ArrayList<Vertex>()), onlyRep);
      }
    }

    this.mw.onKeyEvent("h");

    t.checkExpect(this.mw.length, 3);
    t.checkExpect(this.mw.width, 3);
    t.checkExpect(this.mw.edges.size(), 12);
    t.checkExpect(this.mw.mst.size(), 8);
    t.checkExpect(this.mw.maze.size(), 3);
    t.checkExpect(this.mw.maze.get(0).size(), 3);
    t.checkExpect(this.mw.maze.get(1).size(), 3);
    t.checkExpect(this.mw.maze.get(2).size(), 3);
    t.checkExpect(this.mw.representatives.size(), 9);

    t.checkExpect(this.mw.maze.get(0).get(0), this.v1);
    t.checkExpect(this.mw.maze.get(0).get(1), this.v2);
    t.checkExpect(this.mw.maze.get(0).get(2), this.v3);
    t.checkExpect(this.mw.maze.get(1).get(0), this.v4);
    t.checkExpect(this.mw.maze.get(1).get(1), this.v5);
    t.checkExpect(this.mw.maze.get(1).get(2), this.v6);
    t.checkExpect(this.mw.maze.get(2).get(0), this.v7);
    t.checkExpect(this.mw.maze.get(2).get(1), this.v8);
    t.checkExpect(this.mw.maze.get(2).get(2), this.v9);

    Vertex onlyRep2 = this.mw.find(this.mw.maze.get(0).get(0), new ArrayList<Vertex>());

    for (ArrayList<Vertex> a : this.mw.maze) {
      for (Vertex v : a) {
        t.checkExpect(this.mw.find(v, new ArrayList<Vertex>()), onlyRep2);
      }
    }

    ArrayList<Integer> expectedWeights = new ArrayList<Integer>(
        Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
    ArrayList<Integer> actualWeights = new ArrayList<Integer>();
    t.checkExpect(this.mw.edges.size(), 12);
    t.checkExpect(this.mw.edges.size(), expectedWeights.size());
    for (Edge e : this.mw.edges) {
      t.checkExpect(expectedWeights.contains(e.weight), true);
      actualWeights.add(e.weight);
    }
    for (Integer i : expectedWeights) {
      t.checkExpect(actualWeights.contains(i), true);
    }

    ArrayList<Edge> actualHorizontalEdges = new ArrayList<Edge>(
        Arrays.asList(this.mw.edges.get(0), this.mw.edges.get(2), this.mw.edges.get(5),
            this.mw.edges.get(7), this.mw.edges.get(10), this.mw.edges.get(11)));
    ArrayList<Edge> actualVerticalEdges = new ArrayList<Edge>(
        Arrays.asList(this.mw.edges.get(1), this.mw.edges.get(3), this.mw.edges.get(4),
            this.mw.edges.get(6), this.mw.edges.get(8), this.mw.edges.get(9)));

    ArrayList<Integer> lowerWeights = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4, 5));
    ArrayList<Integer> upperWeights = new ArrayList<Integer>(Arrays.asList(6, 7, 8, 9, 10, 11));

    for (Edge e : actualHorizontalEdges) {
      t.checkExpect(lowerWeights.contains(e.weight), true);
    }
    for (Edge e : actualVerticalEdges) {
      t.checkExpect(upperWeights.contains(e.weight), true);
    }

    t.checkExpect(this.mw.time, 0);
    t.checkExpect(this.mw.mode, new TextImage("", 15, Color.BLACK));
    t.checkExpect(this.mw.searching, false);
    t.checkExpect(this.mw.finished, false);
    t.checkExpect(this.mw.alreadySeen, new LinkedList<Vertex>());
    t.checkExpect(this.mw.alreadySeen2, new LinkedList<Vertex>());
    t.checkExpect(this.mw.cameFromEdge, new HashMap<Vertex, Edge>());
    t.checkExpect(this.mw.path, new ArrayList<Vertex>());
    t.checkExpect(this.mw.path2, new ArrayList<Vertex>());

    this.mw.onKeyEvent("v");

    t.checkExpect(this.mw.length, 3);
    t.checkExpect(this.mw.width, 3);
    t.checkExpect(this.mw.edges.size(), 12);
    t.checkExpect(this.mw.mst.size(), 8);
    t.checkExpect(this.mw.maze.size(), 3);
    t.checkExpect(this.mw.maze.get(0).size(), 3);
    t.checkExpect(this.mw.maze.get(1).size(), 3);
    t.checkExpect(this.mw.maze.get(2).size(), 3);
    t.checkExpect(this.mw.representatives.size(), 9);

    t.checkExpect(this.mw.maze.get(0).get(0), this.v1);
    t.checkExpect(this.mw.maze.get(0).get(1), this.v2);
    t.checkExpect(this.mw.maze.get(0).get(2), this.v3);
    t.checkExpect(this.mw.maze.get(1).get(0), this.v4);
    t.checkExpect(this.mw.maze.get(1).get(1), this.v5);
    t.checkExpect(this.mw.maze.get(1).get(2), this.v6);
    t.checkExpect(this.mw.maze.get(2).get(0), this.v7);
    t.checkExpect(this.mw.maze.get(2).get(1), this.v8);
    t.checkExpect(this.mw.maze.get(2).get(2), this.v9);

    Vertex onlyRep3 = this.mw.find(this.mw.maze.get(0).get(0), new ArrayList<Vertex>());

    for (ArrayList<Vertex> a : this.mw.maze) {
      for (Vertex v : a) {
        t.checkExpect(this.mw.find(v, new ArrayList<Vertex>()), onlyRep3);
      }
    }

    ArrayList<Integer> actualWeights2 = new ArrayList<Integer>();
    t.checkExpect(this.mw.edges.size(), 12);
    t.checkExpect(this.mw.edges.size(), expectedWeights.size());
    for (Edge e : this.mw.edges) {
      t.checkExpect(expectedWeights.contains(e.weight), true);
      actualWeights2.add(e.weight);
    }
    for (Integer i : expectedWeights) {
      t.checkExpect(actualWeights2.contains(i), true);
    }

    ArrayList<Edge> actualHorizontalEdges2 = new ArrayList<Edge>(
        Arrays.asList(this.mw.edges.get(0), this.mw.edges.get(2), this.mw.edges.get(5),
            this.mw.edges.get(7), this.mw.edges.get(10), this.mw.edges.get(11)));
    ArrayList<Edge> actualVerticalEdges2 = new ArrayList<Edge>(
        Arrays.asList(this.mw.edges.get(1), this.mw.edges.get(3), this.mw.edges.get(4),
            this.mw.edges.get(6), this.mw.edges.get(8), this.mw.edges.get(9)));

    for (Edge e : actualHorizontalEdges2) {
      t.checkExpect(upperWeights.contains(e.weight), true);
    }
    for (Edge e : actualVerticalEdges2) {
      t.checkExpect(lowerWeights.contains(e.weight), true);
    }

    t.checkExpect(this.mw.time, 0);
    t.checkExpect(this.mw.mode, new TextImage("", 15, Color.BLACK));
    t.checkExpect(this.mw.searching, false);
    t.checkExpect(this.mw.finished, false);
    t.checkExpect(this.mw.alreadySeen, new LinkedList<Vertex>());
    t.checkExpect(this.mw.alreadySeen2, new LinkedList<Vertex>());
    t.checkExpect(this.mw.cameFromEdge, new HashMap<Vertex, Edge>());
    t.checkExpect(this.mw.path, new ArrayList<Vertex>());
    t.checkExpect(this.mw.path2, new ArrayList<Vertex>());

    this.initData();

    this.mw.onKeyEvent("b");

    t.checkExpect(this.mw.length, 3);
    t.checkExpect(this.mw.width, 3);
    t.checkExpect(this.mw.edges.size(), 12);
    t.checkExpect(this.mw.mst.size(), 8);
    t.checkExpect(this.mw.maze.size(), 3);
    t.checkExpect(this.mw.maze.get(0).size(), 3);
    t.checkExpect(this.mw.maze.get(1).size(), 3);
    t.checkExpect(this.mw.maze.get(2).size(), 3);
    t.checkExpect(this.mw.representatives.size(), 9);

    t.checkExpect(this.mw.maze.get(0).get(0), this.v1);
    t.checkExpect(this.mw.maze.get(0).get(1), this.v2);
    t.checkExpect(this.mw.maze.get(0).get(2), this.v3);
    t.checkExpect(this.mw.maze.get(1).get(0), this.v4);
    t.checkExpect(this.mw.maze.get(1).get(1), this.v5);
    t.checkExpect(this.mw.maze.get(1).get(2), this.v6);
    t.checkExpect(this.mw.maze.get(2).get(0), this.v7);
    t.checkExpect(this.mw.maze.get(2).get(1), this.v8);
    t.checkExpect(this.mw.maze.get(2).get(2), this.v9);

    Vertex onlyRep4 = this.mw.find(this.mw.maze.get(0).get(0), new ArrayList<Vertex>());

    for (ArrayList<Vertex> a : this.mw.maze) {
      for (Vertex v : a) {
        t.checkExpect(this.mw.find(v, new ArrayList<Vertex>()), onlyRep4);
      }
    }

    t.checkExpect(this.mw.time, 0);
    t.checkExpect(this.mw.mode,
        new TextImage("currently using breadth-first search", 15, Color.BLACK));
    t.checkExpect(this.mw.searching, true);
    t.checkExpect(this.mw.finished, false);
    Queue<Vertex> wl = new Queue<Vertex>();
    wl.add(this.v1);
    t.checkExpect(this.mw.worklist, wl);
    HashMap<Vertex, Edge> cfe = new HashMap<Vertex, Edge>();
    cfe.put(this.v1, new Edge(0, this.v1, this.v1));
    t.checkExpect(this.mw.alreadySeen, new LinkedList<Vertex>());
    t.checkExpect(this.mw.alreadySeen2, new LinkedList<Vertex>());
    t.checkExpect(this.mw.worklist, wl);
    t.checkExpect(this.mw.cameFromEdge, cfe);
    t.checkExpect(this.mw.path, new ArrayList<Vertex>());
    t.checkExpect(this.mw.path2, new ArrayList<Vertex>());
    t.checkExpect(this.mw.maze.get(0).get(0).color, Color.GREEN);
    t.checkExpect(this.mw.maze.get(0).get(1).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(0).get(2).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(1).get(0).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(1).get(1).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(1).get(2).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(2).get(0).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(2).get(1).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(2).get(2).color, Color.RED);

    this.mw.onKeyEvent("r");

    t.checkExpect(this.mw.length, 3);
    t.checkExpect(this.mw.width, 3);
    t.checkExpect(this.mw.edges.size(), 12);
    t.checkExpect(this.mw.mst.size(), 8);
    t.checkExpect(this.mw.maze.size(), 3);
    t.checkExpect(this.mw.maze.get(0).size(), 3);
    t.checkExpect(this.mw.maze.get(1).size(), 3);
    t.checkExpect(this.mw.maze.get(2).size(), 3);
    t.checkExpect(this.mw.representatives.size(), 9);

    t.checkExpect(this.mw.maze.get(0).get(0), this.v1);
    t.checkExpect(this.mw.maze.get(0).get(1), this.v2);
    t.checkExpect(this.mw.maze.get(0).get(2), this.v3);
    t.checkExpect(this.mw.maze.get(1).get(0), this.v4);
    t.checkExpect(this.mw.maze.get(1).get(1), this.v5);
    t.checkExpect(this.mw.maze.get(1).get(2), this.v6);
    t.checkExpect(this.mw.maze.get(2).get(0), this.v7);
    t.checkExpect(this.mw.maze.get(2).get(1), this.v8);
    t.checkExpect(this.mw.maze.get(2).get(2), this.v9);

    Vertex onlyRep5 = this.mw.find(this.mw.maze.get(0).get(0), new ArrayList<Vertex>());

    for (ArrayList<Vertex> a : this.mw.maze) {
      for (Vertex v : a) {
        t.checkExpect(this.mw.find(v, new ArrayList<Vertex>()), onlyRep5);
      }
    }

    t.checkExpect(this.mw.time, 0);
    t.checkExpect(this.mw.mode, new TextImage("", 15, Color.BLACK));
    t.checkExpect(this.mw.searching, false);
    t.checkExpect(this.mw.finished, false);
    t.checkExpect(this.mw.alreadySeen, new LinkedList<Vertex>());
    t.checkExpect(this.mw.alreadySeen2, new LinkedList<Vertex>());
    t.checkExpect(this.mw.cameFromEdge, new HashMap<Vertex, Edge>());
    t.checkExpect(this.mw.path, new ArrayList<Vertex>());
    t.checkExpect(this.mw.path2, new ArrayList<Vertex>());
    t.checkExpect(this.mw.maze.get(0).get(0).color, Color.GREEN);
    t.checkExpect(this.mw.maze.get(0).get(1).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(0).get(2).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(1).get(0).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(1).get(1).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(1).get(2).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(2).get(0).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(2).get(1).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(2).get(2).color, Color.RED);

    this.mw.onKeyEvent("d");

    t.checkExpect(this.mw.length, 3);
    t.checkExpect(this.mw.width, 3);
    t.checkExpect(this.mw.edges.size(), 12);
    t.checkExpect(this.mw.mst.size(), 8);
    t.checkExpect(this.mw.maze.size(), 3);
    t.checkExpect(this.mw.maze.get(0).size(), 3);
    t.checkExpect(this.mw.maze.get(1).size(), 3);
    t.checkExpect(this.mw.maze.get(2).size(), 3);
    t.checkExpect(this.mw.representatives.size(), 9);

    t.checkExpect(this.mw.maze.get(0).get(0), this.v1);
    t.checkExpect(this.mw.maze.get(0).get(1), this.v2);
    t.checkExpect(this.mw.maze.get(0).get(2), this.v3);
    t.checkExpect(this.mw.maze.get(1).get(0), this.v4);
    t.checkExpect(this.mw.maze.get(1).get(1), this.v5);
    t.checkExpect(this.mw.maze.get(1).get(2), this.v6);
    t.checkExpect(this.mw.maze.get(2).get(0), this.v7);
    t.checkExpect(this.mw.maze.get(2).get(1), this.v8);
    t.checkExpect(this.mw.maze.get(2).get(2), this.v9);

    Vertex onlyRep6 = this.mw.find(this.mw.maze.get(0).get(0), new ArrayList<Vertex>());

    for (ArrayList<Vertex> a : this.mw.maze) {
      for (Vertex v : a) {
        t.checkExpect(this.mw.find(v, new ArrayList<Vertex>()), onlyRep6);
      }
    }

    t.checkExpect(this.mw.time, 0);
    t.checkExpect(this.mw.mode,
        new TextImage("currently using depth-first search", 15, Color.BLACK));
    t.checkExpect(this.mw.searching, true);
    t.checkExpect(this.mw.finished, false);
    Stack<Vertex> wl2 = new Stack<Vertex>();
    wl2.add(this.v1);
    t.checkExpect(this.mw.worklist, wl2);
    HashMap<Vertex, Edge> cfe2 = new HashMap<Vertex, Edge>();
    cfe2.put(this.v1, new Edge(0, this.v1, this.v1));
    t.checkExpect(this.mw.alreadySeen, new LinkedList<Vertex>());
    t.checkExpect(this.mw.alreadySeen2, new LinkedList<Vertex>());
    t.checkExpect(this.mw.worklist, wl2);
    t.checkExpect(this.mw.cameFromEdge, cfe2);
    t.checkExpect(this.mw.path, new ArrayList<Vertex>());
    t.checkExpect(this.mw.path2, new ArrayList<Vertex>());
    t.checkExpect(this.mw.maze.get(0).get(0).color, Color.GREEN);
    t.checkExpect(this.mw.maze.get(0).get(1).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(0).get(2).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(1).get(0).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(1).get(1).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(1).get(2).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(2).get(0).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(2).get(1).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(2).get(2).color, Color.RED);

    this.mw.onKeyEvent("n");

    t.checkExpect(this.mw.length, 3);
    t.checkExpect(this.mw.width, 3);
    t.checkExpect(this.mw.edges.size(), 12);
    t.checkExpect(this.mw.mst.size(), 8);
    t.checkExpect(this.mw.maze.size(), 3);
    t.checkExpect(this.mw.maze.get(0).size(), 3);
    t.checkExpect(this.mw.maze.get(1).size(), 3);
    t.checkExpect(this.mw.maze.get(2).size(), 3);
    t.checkExpect(this.mw.representatives.size(), 9);

    t.checkExpect(this.mw.maze.get(0).get(0), this.v1);
    t.checkExpect(this.mw.maze.get(0).get(1), this.v2);
    t.checkExpect(this.mw.maze.get(0).get(2), this.v3);
    t.checkExpect(this.mw.maze.get(1).get(0), this.v4);
    t.checkExpect(this.mw.maze.get(1).get(1), this.v5);
    t.checkExpect(this.mw.maze.get(1).get(2), this.v6);
    t.checkExpect(this.mw.maze.get(2).get(0), this.v7);
    t.checkExpect(this.mw.maze.get(2).get(1), this.v8);
    t.checkExpect(this.mw.maze.get(2).get(2), this.v9);

    Vertex onlyRep7 = this.mw.find(this.mw.maze.get(0).get(0), new ArrayList<Vertex>());

    for (ArrayList<Vertex> a : this.mw.maze) {
      for (Vertex v : a) {
        t.checkExpect(this.mw.find(v, new ArrayList<Vertex>()), onlyRep7);
      }
    }

    t.checkExpect(this.mw.time, 0);
    t.checkExpect(this.mw.mode, new TextImage("", 15, Color.BLACK));
    t.checkExpect(this.mw.searching, false);
    t.checkExpect(this.mw.finished, false);
    t.checkExpect(this.mw.alreadySeen, new LinkedList<Vertex>());
    t.checkExpect(this.mw.alreadySeen2, new LinkedList<Vertex>());
    t.checkExpect(this.mw.cameFromEdge, new HashMap<Vertex, Edge>());
    t.checkExpect(this.mw.path, new ArrayList<Vertex>());
    t.checkExpect(this.mw.path2, new ArrayList<Vertex>());
    t.checkExpect(this.mw.maze.get(0).get(0).color, Color.GREEN);
    t.checkExpect(this.mw.maze.get(0).get(1).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(0).get(2).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(1).get(0).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(1).get(1).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(1).get(2).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(2).get(0).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(2).get(1).color, Color.GRAY);
    t.checkExpect(this.mw.maze.get(2).get(2).color, Color.RED);

    this.initData();

    this.mw.onKeyEvent("b");
    this.mw.onKeyEvent("p");

    MazeWorld current = this.mw;

    t.checkExpect(this.mw.paused, true);
    t.checkExpect(this.mw.pause, new TextImage("the search is paused", 15, Color.BLACK));
    t.checkExpect(this.mw.searching, true);

    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();

    t.checkExpect(this.mw, current);
  }

  // test the void reconstruct(Vertex, Vertex) method
  void testReconstruct(Tester t) {
    this.initData();

    HashMap<Vertex, Edge> cfe = new HashMap<Vertex, Edge>();
    ArrayList<Vertex> mwPath = new ArrayList<Vertex>();

    t.checkExpect(this.mw.cameFromEdge, cfe);
    t.checkExpect(this.mw.path, mwPath);

    this.mw.onKeyEvent("b");

    this.mw.onTick();

    cfe.put(this.v1, new Edge(0, this.v1, this.v1));
    cfe.put(this.v4, new Edge(1, this.v1, this.v4));

    t.checkExpect(this.mw.cameFromEdge, cfe);
    t.checkExpect(this.mw.path, mwPath);

    this.initData();

    this.mw.onKeyEvent("b");

    // After this many ticks, reconstruct gets called on the MazeWorld
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();

    mwPath.add(this.v1);
    mwPath.add(this.v4);
    mwPath.add(this.v5);
    mwPath.add(this.v6);
    mwPath.add(this.v9);

    t.checkExpect(this.mw.path, mwPath);

    this.initData();

    HashMap<Vertex, Edge> cfe1 = new HashMap<Vertex, Edge>();
    ArrayList<Vertex> mwPath1 = new ArrayList<Vertex>();

    t.checkExpect(this.mw.cameFromEdge, cfe1);
    t.checkExpect(this.mw.path, mwPath1);

    this.mw.onKeyEvent("d");

    this.mw.onTick();

    cfe1.put(this.v1, new Edge(0, this.v1, this.v1));
    cfe1.put(this.v4, new Edge(1, this.v1, this.v4));

    t.checkExpect(this.mw.cameFromEdge, cfe1);
    t.checkExpect(this.mw.path, mwPath1);

    this.initData();

    this.mw.onKeyEvent("d");

    // After this many ticks, reconstruct gets called on the MazeWorld
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();
    this.mw.onTick();

    mwPath1.add(this.v1);
    mwPath1.add(this.v4);
    mwPath1.add(this.v5);
    mwPath1.add(this.v6);
    mwPath1.add(this.v9);

    t.checkExpect(this.mw.path, mwPath1);
  }

}