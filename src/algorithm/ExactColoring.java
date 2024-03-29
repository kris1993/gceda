package algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExactColoring<T> extends Coloring<T> {

	private List<TreeState<T>> colored;

	/*
	 * Contiene la cantidad de colores distintos usados en el grafo, 0 en el
	 * caso de que el mismo no este completamente coloreado o si el grafo esta
	 * vacio
	 */
	private int usedColors;

	public ExactColoring(Graph<T> graph) {
		super(graph);
		this.colored = new ArrayList<TreeState<T>>();
		this.usedColors = 0;
	}

	public Graph<TreeState<T>> perfectColoring(boolean makeTree) {
		available.add(0);

		Graph<TreeState<T>> tree = new Graph<TreeState<T>>();
		TreeState<T> start = new TreeState<T>(null, -1);
		if (makeTree)
			tree.addVertex(start);
		for (T info : graph.DFS()) {
			TreeState<T> neighborState = perfectColoring(info, tree, makeTree);
			if (makeTree)
				tree.addEdge(start, neighborState);
		}

		for (TreeState<T> state : colored) {
			graph.setColor(state.getInfo(), state.getColor());
		}
		return tree;
	}

	private TreeState<T> perfectColoring(T info, Graph<TreeState<T>> tree,
			boolean makeTree) {
		color(info);

		TreeState<T> state = new TreeState<T>(info, graph.getColor(info));
		if (makeTree)
			tree.addVertex(state);
		for (T other : graph.DFS()) {
			// Si la cantidad de colores disponibles iguala o supera
			// la cantidad de colores usados en la mejor solucion
			// hasta ahora, no sigue verificando ese nodo
			if (usedColors != 0 && colorsInUse() == usedColors) {
				discolor(info);
				return state;
			}
			// Si no se coloreo aun
			else if (graph.getColor(other) == -1) {
				TreeState<T> neighborState = perfectColoring(other, tree,
						makeTree);
				if (makeTree)
					tree.addEdge(state, neighborState);
			}
		}

		// Si coloreo todos los vertices y todavia no habia llegado a una
		// solucion o llego a una mejor que la que ya existia la reemplaza
		if (hasAllColored() && (usedColors == 0 || colorsInUse() < usedColors)) {
			backUp();
		}
		discolor(info);
		return state;
	}

	private int colorsInUse() {
		int acum = 0;
		for (int i = 0; i < nodesPerColor.size(); i++)
			if (nodesPerColor.get(i) > 0)
				acum++;
		return acum;
	}

	/*
	 * Verifica si coloreo a todos los nodos
	 */
	private boolean hasAllColored() {
		int vertexCount = graph.vertexCount();
		int acum = 0;
		for (int color : nodesPerColor) {
			acum += color;
		}
		return acum == vertexCount;
	}

	/*
	 * Contiene por cada nodo, su informacion y su color
	 */
	private void backUp() {
		colored.clear();
		Set<Integer> distinctColors = new HashSet<Integer>();
		for (T info : graph.DFS()) {
			colored.add(new TreeState<T>(info, graph.getColor(info)));
			distinctColors.add(graph.getColor(info));
		}
		usedColors = distinctColors.size();
	}

	public int getKNumber() {
		return usedColors;
	}
}
