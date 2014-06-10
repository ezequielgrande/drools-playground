/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.examples.traindesign.app;

import org.drools.planner.config.XmlSolverFactory;
import org.drools.planner.core.Solver;
import org.drools.planner.examples.common.app.CommonApp;
import org.drools.planner.examples.common.persistence.AbstractSolutionImporter;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.common.swingui.SolutionPanel;
import org.drools.planner.examples.traindesign.persistence.TrainDesignDaoImpl;
import org.drools.planner.examples.traindesign.persistence.TrainDesignSolutionImporter;
import org.drools.planner.examples.traindesign.swingui.TrainDesignPanel;

/**
 * TODO This example is unfinished and does not work yet.
 */
public class TrainDesignApp extends CommonApp {

    public static final String SOLVER_CONFIG
            = "/org/drools/planner/examples/traindesign/solver/trainDesignSolverConfig.xml";

    public static void main(String[] args) {
        new TrainDesignApp().init();
    }

    @Override
    protected Solver createSolver() {
        XmlSolverFactory solverFactory = new XmlSolverFactory();
        solverFactory.configure(SOLVER_CONFIG);
        return solverFactory.buildSolver();
    }

    @Override
    protected SolutionPanel createSolutionPanel() {
        return new TrainDesignPanel();
    }

    @Override
    protected SolutionDao createSolutionDao() {
        return new TrainDesignDaoImpl();
    }

    @Override
    protected AbstractSolutionImporter createSolutionImporter() {
        return new TrainDesignSolutionImporter();
    }

}
