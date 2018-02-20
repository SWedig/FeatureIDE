/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2017  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 *
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */
package org.prop4j.solver.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.solver.ISatProblem;

/**
 * Abstract class representing a satisfiability problem in CNF that is given as input for the sat solver.
 *
 * @author Joshua Sprey
 */
public class SatProblem implements ISatProblem {

	protected Node root;
	protected HashMap<Object, Integer> varToInt = new HashMap<>();
	protected Object[] intToVar;
	protected HashMap<Node, Integer> clauseToInt = new HashMap<>();
	protected Node[] intToClause;

	/**
	 * Initiates the problem with a root node and a given feature list that represent variables.
	 *
	 * @param rootNode
	 */
	public SatProblem(Node rootNode, Collection<?> featureList) {
		intToVar = new Object[featureList.size() + 1];
		intToClause = new Node[rootNode.getChildren().length];
		root = rootNode;

		if (!root.isConjunctiveNormalForm()) {
			throw new IllegalStateException("The given root node to create a sat problem need to be in conjunctive normal form.");
		}

		// Create mapping from index to clauses starting from 0
		int indexClauses = 0;
		for (final Node node : rootNode.getChildren()) {
			clauseToInt.put(node, indexClauses);
			intToClause[indexClauses++] = node;
		}

		// Create mapping from index to variables starting from 1 to represent 1 as variable 1 is true and -1 as variable 1 is false.
		int indexVariables = 0;
		for (final Object feature : featureList) {
			final String name = feature.toString();
			if (name == null) {
				throw new RuntimeException();
			}
			varToInt.put(name, ++indexVariables);
			intToVar[indexVariables] = name;
		}
	}

	public SatProblem(Node rootNode) {
		this(rootNode, getDistinctVariableObjects(rootNode));
	}

	public static Set<Object> getDistinctVariableObjects(Node cnf) {
		final HashSet<Object> result = new HashSet<>();
		for (final Node clause : cnf.getChildren()) {
			final Node[] literals = clause.getChildren();
			for (int i = 0; i < literals.length; i++) {
				if (!result.contains(((Literal) literals[i]).var)) {
					result.add(((Literal) literals[i]).var);
				}
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.prop4j.solver.ISolverProblem#addFormula(org.prop4j.Node)
	 */
	@Override
	public void addFormula(Node formula) {
		// TODO ATTRIBUTES todo :p

	}

	/*
	 * (non-Javadoc)
	 * @see org.prop4j.solver.ISolverProblem#getRoot()
	 */
	@Override
	public Node getRoot() {
		return root;
	}

	/*
	 * (non-Javadoc)
	 * @see org.prop4j.solver.ISolverProblem#getSignedIndexOfVariable(java.lang.Object)
	 */
	@Override
	public int getSignedIndexOfVariable(Literal l) {
		return l.positive ? varToInt.get(l.var) : -varToInt.get(l.var);
	}

	/*
	 * (non-Javadoc)
	 * @see org.prop4j.solver.ISolverProblem#getIndexOfVariable(java.lang.Object)
	 */
	@Override
	public int getIndexOfVariable(Object variable) {
		final Integer varInt = varToInt.get(variable);
		return varInt == null ? 0 : varInt;
	}

	/*
	 * (non-Javadoc)
	 * @see org.prop4j.solver.ISolverProblem#getIndexOfClause(java.lang.Object)
	 */
	@Override
	public int getIndexOfClause(Node clause) {
		final Integer clauseInt = clauseToInt.get(clause);
		return clauseInt == null ? -1 : clauseInt;
	}

	/*
	 * (non-Javadoc)
	 * @see org.prop4j.solver.ISolverProblem#getClauseOfIndex(java.lang.Object)
	 */
	@Override
	public Node getClauseOfIndex(int index) {
		if ((index > intToClause.length)) {
			return null;
		}
		return intToClause[index];
	}

	/*
	 * (non-Javadoc)
	 * @see org.prop4j.solver.ISolverProblem#getVariableOfIndex(int)
	 */
	@Override
	public Object getVariableOfIndex(int index) {
		if ((index > intToVar.length)) {
			return null;
		}
		return intToVar[Math.abs(index)];
	}

	/*
	 * (non-Javadoc)
	 * @see org.prop4j.solver.ISolverProblem#getNumberOfVariables()
	 */
	@Override
	public Integer getNumberOfVariables() {
		return intToVar.length - 1;
	}

	/*
	 * (non-Javadoc)
	 * @see org.prop4j.solver.ISolverProblem#getClauses()
	 */
	@Override
	public Node[] getClauses() {
		return intToClause;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SatProblem[" + root.toString() + "]";
	}
}
