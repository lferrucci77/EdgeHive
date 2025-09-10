EdgeHive: Tool for Decentralized Resource Management in Edge Computing
Overview

EdgeHive is a tool designed to optimize the resource management and workload balancing of decentralized edge computing systems. It leverages a Marginal Computing Cost per User (MCU) model to facilitate self-organizing behavior in Edge Data Centers (EDCs) based on a collaborative approach. This tool extends the capabilities of the PureEdgeSim simulator to manage the distribution of workloads, data, and applications on edge devices.

The primary goal of EdgeHive is to balance the load across edge devices, minimize latency, and ensure the efficient distribution of resources in a decentralized system. It does so by evaluating the readiness of EDCs to accommodate new users based on a cost-driven framework. Through simulations, the system optimizes resource allocation and reduces the overhead of managing decentralized infrastructures.

Key Features

Collaborative Resource Management: Using the MCU metric, EdgeHive enables edge devices to autonomously balance their workloads and manage resources in a collaborative environment. EDCs can decide whether to accept or offload users based on resource availability and latency constraints.

Decentralized Workload Balancing: Unlike traditional centralized systems, EdgeHive uses a decentralized approach, where each EDC works autonomously while coordinating with nearby nodes. This reduces the risk of single points of failure and optimizes scalability.

Policy-Driven Approach: EdgeHive allows EDCs to define customizable policies on how to handle additional user requests, which is crucial in resource-constrained environments. Policies are influenced by factors like latency, resource availability, and marginal cost for each user.

Real-Time Latency Management: The system ensures that all operations respect predefined latency limits for each application, improving Quality of Experience (QoE) for users.

Resource Efficiency: Through load balancing, EdgeHive reduces the number of active application instances, optimizing resource use without sacrificing performance.

Simulation with PureEdgeSim

EdgeHive integrates with PureEdgeSim, a state-of-the-art edge computing simulator, to evaluate the effectiveness of its workload balancing mechanisms. The simulator allows the testing of various scenarios involving edge devices, user distribution, and application workload characteristics.

Real-World Datasets: Simulations use datasets like the Alibaba Trace Dataset, which consists of microservices data and real-world user behavior, providing insights into how the system performs under different loads and configurations.

Experimental Setup: The simulations involve 125 edge nodes deployed in a 1.2 km² area, with workloads derived from over 20,000 microservices, demonstrating the scalability of the system.

Core Algorithms

Marginal Computing Cost per User (MCU):

MCU quantifies the residual capacity of an EDC to accept additional users. It represents the readiness of the system to accommodate new users based on its current load and resource availability.

The system’s performance is guided by the relationship between the average user cost and the marginal computing cost of a new user.

Collaborative Load Balancing Algorithm:

EDCs assess their own workload and the marginal cost of accommodating additional users. Based on this assessment, EDCs decide whether to offload users to neighboring edge nodes.

The decision process is influenced by factors like latency, resource usage, and the willingness of neighboring EDCs to accept additional users.

User Migration:

Users can be migrated between edge devices to balance the load. This migration occurs based on the capacity of neighboring devices to handle the additional users while respecting latency constraints.

Partial Migration:

EdgeHive supports the partial migration of users, transferring subsets of users from one node to another instead of performing complete migration. This helps reduce the system's migration overhead.

Simulation Results

EdgeHive's framework was evaluated through extensive experiments using different user distribution models (e.g., Uniform Random and Random) and various edge configurations. Key results include:

Load Balancing: The system demonstrated improved load distribution across edge devices compared to baseline strategies. This is validated using Lorenz curves and Gini coefficients to measure inequality in resource distribution.

Impact of User Migrations: The system's ability to efficiently migrate users was analyzed. As expected, higher values of MCU resulted in fewer migrations, contributing to system stability and performance.

Latency Management: EdgeHive kept the latency within acceptable bounds by selectively migrating users based on their current load and the MCU cost, ensuring that migrations did not introduce excessive delays.

Conclusion

EdgeHive provides a scalable and efficient solution for decentralized resource management in edge computing environments. By integrating a policy-driven marginal cost model, the tool ensures optimal workload distribution while adhering to latency constraints. Its implementation in PureEdgeSim highlights its effectiveness in improving resource utilization and reducing the operational cost of edge systems.

EdgeHive’s approach is adaptable to real-world applications, particularly in environments with high user demands, such as autonomous vehicles, healthcare, and industrial IoT. Future improvements will focus on incorporating more dynamic models and real-time adaptations for user mobility and resource allocation.

   
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
   
References

Mechalikh, C., Taktak, H., Moussa, F.: PureEdgeSim: A Simulation Toolkit for Performance Evaluation of Cloud, Fog, and Pure Edge Computing Environments. The 2019 International Conference on High Performance Computing & Simulation (2019) 700-707

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) [![Build Status](https://travis-ci.com/CharafeddineMechalikh/PureEdgeSim.svg?branch=master)](https://travis-ci.com/CharafeddineMechalikh/PureEdgeSim) [![Build status](https://ci.appveyor.com/api/projects/status/u6hwmktmbji8utnf?svg=true)](https://ci.appveyor.com/project/CharafeddineMechalikh/pureedgesim) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/25ee278611014a9bb242297480703cf9)](https://www.codacy.com/manual/CharafeddineMechalikh/PureEdgeSim?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=CharafeddineMechalikh/PureEdgeSim&amp;utm_campaign=Badge_Grade) [![Maintainability](https://api.codeclimate.com/v1/badges/a1ffecb5230fc5771b93/maintainability)](https://codeclimate.com/github/CharafeddineMechalikh/PureEdgeSim/maintainability) [![codebeat badge](https://codebeat.co/badges/bbe172a2-1169-4bbe-b6a6-0505631babc6)](https://codebeat.co/projects/github-com-charafeddinemechalikh-pureedgesim-master) [![Maven Central](https://img.shields.io/maven-central/v/com.mechalikh/pureedgesim.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.mechalikh%22%20AND%20a:%22pureedgesim%22)
