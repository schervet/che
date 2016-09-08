/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

import {EnvironmentManager} from './environment-manager';

/**
 * This is the implementation of environment manager that handles the docker file format of environment.
 *
 * Format sample and specific description:
 * //TODO
 *
 */
export class DockerFileEnvironmentManager extends EnvironmentManager {

  constructor() {
    super();
    //TODO define file specific stuff
  }

  /**
   * Retrieves the list of machines.
   *
   * @param environment environment's configuration
   * @returns {Array} list of machines defined in environment
   */
  getMachines(environment) {
    return [];
  }

  /**
   * Provides the environment configuration based on machines format.
   *
   * @param environment origin of the environment to be edited
   * @param machines the list of machines
   * @returns environment's configuration
   */
  getEnvironment(environment, machines) {
    return {};
  }
}
