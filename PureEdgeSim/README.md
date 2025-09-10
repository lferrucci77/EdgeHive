## 1. Background

   
## 2. PureEdgeSim Architecture

PureEdgeSim enables the simulation of resource management strategies and allows to evaluate the performance of Cloud, Edge, and Mist computing environments. It grantees high scalability by enabling the simulation of thousands of devices. Besides, it supports the Edge devices heterogeneity (i.e. whether this device is mobile or not, whether battery-powered or not, different  applications requirements: tasks file size, tasks CPU utilization,and latency requirement, etc.) 

![Environment](https://github.com/CharafeddineMechalikh/PureEdgeSim/blob/master/PureEdgeSim/files/scenario.JPG)

A simple representation of the simulation scenarios

It provides a task orchestrator module that orchestrates the tasks and enables the multi-tiers simulations scenarios where many computing paradigms can be used in conjunction with one another. Besides, it provides an even more realistic network model (as compared to state of the art simulators) that continuously changes the allocated bandwidth for each task being transferred depending on the network traffic. 

It consists of the following 7 modules:

*   Scenario Manager, that loads the simulation parameters and the user scenario from the input files (`.xml` and `.prop` files in `/settings/` folder). It consists of two classes, the File Parser that checks the input files and loads the simulation parameters, and the Simulation Parameters class which represents a placeholder for the different parameters.


*   Simulation Manager, that initiates the simulation environment, schedules all the events and generates the output. It consists of two important classes, the Simulation Manager class which manages the simulation, schedules the tasks generation, etc. The Simulation Logger class that generates the simulation output saves it in comma-separated value (CSV) format in order to easily exploit them later using any spreadsheet editor (e.g., Microsoft Excel...).


*   Data Centers Manager: it generates and manages all the data centers and devices (i.e., Cloud, Edge or Mist). It consists of two classes: the Data Center class, that contains the specific properties of Edge devices such as the location, the mobility, the energy source, and the capacity/remaining energy if it is battery-powered. The second class is the Server Manager which generates the needed servers and Edge devices, their hosts and their virtual machines.


*   Tasks Generator which is behind the tasks generation, -currently- it assigns an application such as e-health, smart-home, and augmented-reality (that can be defined in `settings/applications.xml` file) to each Edge device. Then, it will generates the needed tasks according to the assigned type, which guarantees the heterogeneity of applications.  


*   The Network Module: that consists mainly of the Network Model class.which is behind the transfer of tasks/containers/ request... 


*   The Tasks Orchestrator, which is the decision maker, where the user can define the orchestration algorithm. 


*   The Location Manager, which generates the mobility path of mobile devices.
   
![Architecture](https://github.com/CharafeddineMechalikh/PureEdgeSim/blob/master/PureEdgeSim/files/modules.PNG)

PureEdgeSim architecture

References

Mechalikh, C., Taktak, H., Moussa, F.: PureEdgeSim: A Simulation Toolkit for Performance Evaluation of Cloud, Fog, and Pure Edge Computing Environments. The 2019 International Conference on High Performance Computing & Simulation (2019) 700-707

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) [![Build Status](https://travis-ci.com/CharafeddineMechalikh/PureEdgeSim.svg?branch=master)](https://travis-ci.com/CharafeddineMechalikh/PureEdgeSim) [![Build status](https://ci.appveyor.com/api/projects/status/u6hwmktmbji8utnf?svg=true)](https://ci.appveyor.com/project/CharafeddineMechalikh/pureedgesim) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/25ee278611014a9bb242297480703cf9)](https://www.codacy.com/manual/CharafeddineMechalikh/PureEdgeSim?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=CharafeddineMechalikh/PureEdgeSim&amp;utm_campaign=Badge_Grade) [![Maintainability](https://api.codeclimate.com/v1/badges/a1ffecb5230fc5771b93/maintainability)](https://codeclimate.com/github/CharafeddineMechalikh/PureEdgeSim/maintainability) [![codebeat badge](https://codebeat.co/badges/bbe172a2-1169-4bbe-b6a6-0505631babc6)](https://codebeat.co/projects/github-com-charafeddinemechalikh-pureedgesim-master) [![Maven Central](https://img.shields.io/maven-central/v/com.mechalikh/pureedgesim.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.mechalikh%22%20AND%20a:%22pureedgesim%22)
