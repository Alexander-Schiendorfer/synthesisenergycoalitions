package de.uniaugsburg.isse.solvers;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.DoubleParam;
import ilog.opl.IloOplDataSource;
import ilog.opl.IloOplErrorHandler;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplModelSource;
import ilog.opl.IloOplSettings;

import java.util.ArrayList;
import java.util.Collection;

import de.uniaugsburg.isse.solvers.handlers.CplexConstraintValueHandler;

public class CplexSolver implements ConstraintSolver {
	private IloOplFactory factory;
	private IloCplex cplex;
	private IloOplModel model;
	private boolean solved;
	private final Collection<CplexConstraintValueHandler> constraintValueHandlers;
	private int timeLimit;

	public int getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}

	public CplexSolver() {
		constraintValueHandlers = new ArrayList<CplexConstraintValueHandler>();
	}

	public void addConstraintValueHandler(CplexConstraintValueHandler cvh) {
		this.constraintValueHandlers.add(cvh);
	}

	@Override
	public boolean solve(String model, Collection<String> dataFiles) {
		if (this.factory == null)
			this.setUpCplex();

		IloOplErrorHandler handler = this.factory.createOplErrorHandler();
		IloOplModelSource modelSource = this.factory.createOplModelSource(model);
		IloOplSettings settings = this.factory.createOplSettings(handler);

		IloOplModelDefinition modelDefinition = this.factory.createOplModelDefinition(modelSource, settings);

		this.model = this.factory.createOplModel(modelDefinition, this.cplex);
		if (dataFiles != null && !dataFiles.isEmpty()) {
			for (String dataFile : dataFiles) {
				IloOplDataSource dataSource = this.factory.createOplDataSource(dataFile);
				this.model.addDataSource(dataSource);
			}
		}

		this.model.generate();
		try {
			this.solved = this.cplex.solve();
		} catch (IloException e) {
			e.printStackTrace();
			return false;
		}

		if (this.solved) {
			// notify handlers now

			this.model.postProcess();
			for (CplexConstraintValueHandler constraintValueHandler : constraintValueHandlers) {
				constraintValueHandler.process(this.model);
			}

		} else
			for (CplexConstraintValueHandler cvh : constraintValueHandlers)
				cvh.notifyFail();
		return this.solved;
	}

	private void setUpCplex() {
		this.factory = new IloOplFactory();
		IloOplFactory.setDebugMode(false);
		try {
			this.cplex = this.factory.createCplex();
			if (this.timeLimit > 0)
				this.cplex.setParam(DoubleParam.TiLim, this.timeLimit);
			this.cplex.setOut(null);

		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	@Override
	public double getObjective() {
		try {
			return cplex.getObjValue();
		} catch (IloException e) {
			e.printStackTrace();
			return -1;
		}
	}

}
