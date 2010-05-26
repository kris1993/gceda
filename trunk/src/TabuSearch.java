import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TabuSearch<T> extends Coloring<T> {

	private static int MAX_TRIES = 5;
	private static int[] memory;
	private static int j = 4;

	private class Solution {

		Set<State<T>> states;
		/* Indica cual es el nodo que cambio de color */
		int index;

		public Solution(Set<State<T>> states, int index) {
			this.states = states;
			this.index = index;
		}

		public int evaluate() {
			Set<Integer> distinctColors = new HashSet<Integer>();
			for (State<T> state : states)
				distinctColors.add(state.getColor());
			return distinctColors.size();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + index;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Solution other = (Solution) obj;
			if (index != other.index)
				return false;
			return true;
		}

		public Set<Solution> neighbors() {
			List<Integer> backUpQC = new ArrayList<Integer>(quantColor);
			List<Integer> backUpAv = new ArrayList<Integer>(available);
			Set<Solution> set = new HashSet<Solution>();
			int i = 0;
			for (T info : graph.DFS()) {
				if (memory[i] == 0) {
					Set<State<T>> aux = new HashSet<State<T>>();
					changeColor(info, aux);
					// Agrega los estados de los nodos que no fueron
					// modificados ya que hashea por el info
					aux.addAll(states);
					set.add(new Solution(aux, i));
					restore(backUpQC, backUpAv);
				}
				i++;
			}
			return set;
		}

		private void changeColor(T info, Set<State<T>> ans) {
			int oldColor = graph.getColor(info);
			discolor(info);
			int i = 0;
			while (graph.getColor(info) == -1) {
				if (i == available.size())
					available.add(available.get(i - 1) + 1);
				if (available.get(i) != oldColor)
					graph.setColor(info, available.get(i));
				i++;
			}
			int newColor = graph.getColor(info);
			if (newColor == quantColor.size())
				quantColor.add(0);
			quantColor.set(newColor, quantColor.get(newColor) + 1);

			ans.add(new State<T>(info, graph.getColor(info)));

			for (T next : graph.neighborsColor(info)) {
				if (ans.contains(new State<T>(next, -1))) {
					discolor(info);
					color(info);
					ans.remove(new State<T>(info, -1));
					ans.add(new State<T>(info, graph.getColor(info)));
					return;
				} else {
					changeColor(next, ans);
				}
			}
		}

		private void restore(List<Integer> backUpQC, List<Integer> backUpAv) {
			for (State<T> state : states) {
				graph.setColor(state.getInfo(), state.getColor());
			}
			quantColor = new ArrayList<Integer>(backUpQC);
			available = new ArrayList<Integer>(backUpAv);
		}

		// PARA TESTEAR
		public String toString() {
			return states.toString();
		}
	}

	public TabuSearch(Graph<T> graph) {
		super(graph);
	}

	public void coloring() {
		memory = new int[graph.vertexCount()];
		Solution localSolution = initialSolution();
		Solution bestSolution = localSolution;
		boolean change = false;
		int localSolEval = localSolution.evaluate(), bestSolEval = localSolEval;
		for (int i = 0; i < MAX_TRIES; i++) {
			change = false;
			Set<Solution> neighbors = localSolution.neighbors();
			for (Solution neighbor : neighbors) {
				int neighborSolEval = neighbor.evaluate();
				if (localSolEval > neighborSolEval) {
					localSolution = neighbor;
					localSolEval = neighborSolEval;
					change = true;
				}
			}
			if (!change) {
				localSolution = neighbors.iterator().next();
				localSolEval = localSolution.evaluate();
			} else if (bestSolEval > localSolEval) {
				bestSolution = localSolution;
				bestSolEval = localSolEval;
			}
			colorGraph(localSolution);
			updateLists(localSolution.states);
			memory[localSolution.index] = j;
			for (int k = 0; k < memory.length; k++)
				if (memory[k] != 0)
					memory[k]--;
		}
		if (bestSolEval < localSolEval) {
			colorGraph(bestSolution);
		}
	}

	private Solution initialSolution() {
		Set<State<T>> states = new HashSet<State<T>>();
		available.add(0);
		for (T info : graph.DFS()) {
			color(info);
			states.add(new State<T>(info, graph.getColor(info)));
		}
		return new Solution(states, -1);
	}

	private void colorGraph(Solution solution) {
		for (State<T> state : solution.states) {
			graph.setColor(state.getInfo(), state.getColor());
		}
	}

	private void updateLists(Set<State<T>> states) {
		Set<Integer> aux = new HashSet<Integer>();
		for (int i = 0; i < quantColor.size(); i++)
			quantColor.set(i, 0);
		available.clear();
		for (State<T> state : states) {
			aux.add(state.getColor());
			while (state.getColor() >= quantColor.size()) {
				quantColor.add(0);
			}
			quantColor.set(state.getColor(),
					quantColor.get(state.getColor()) + 1);
		}
		available.addAll(aux);
	}

	public int getKNumber() {
		int ans = 0;
		for (int num : quantColor) {
			if (num > 0)
				ans++;
		}
		return ans;
	}
}