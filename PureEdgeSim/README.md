# **EdgeHive: Tool for Decentralized Resource Management in Edge Computing**

## 1. Overview

EdgeHive is a tool designed to optimize the resource management and workload balancing of decentralized edge computing systems. It leverages a **Marginal Computing Cost per User (MCU)** model to facilitate self-organizing behavior in Edge Data Centers (EDCs) based on a collaborative approach. This tool, actually, is implemented on the **PureEdgeSim** simulator to manage the distribution of workloads, data, and applications on edge devices, and to test the validity of the approach.

The primary goal of EdgeHive is to balance the load across edge devices, keeping a predefined threshold on latency, ensuring the efficient distribution of resources in a decentralized system. It does so by evaluating the readiness of EDCs to accommodate new users based on a cost-driven framework. Through simulations, the system optimizes resource allocation and reduces the overhead of managing decentralized infrastructures.

## 2. Key Features

- **Collaborative Resource Management**: Using the MCU metric, EdgeHive enables edge devices to autonomously balance their workloads and manage resources in a collaborative environment. EDCs can decide whether to accept or offload users based on resource availability and latency constraints.

- **Decentralized Workload Balancing**: Unlike traditional centralized systems, EdgeHive uses a decentralized approach, where each EDC works autonomously while coordinating with nearby nodes. This reduces the risk of single points of failure and optimizes scalability.

- **Policy-Driven Approach**: EdgeHive allows EDCs to define customizable policies on how to handle additional user requests, which is crucial in resource-constrained environments. Policies are influenced by factors like latency, resource availability, and marginal cost for each user.

- **Real-Time Latency Management**: The system ensures that all operations respect predefined latency limits for each application, improving Quality of Experience (QoE) for users.

- **Resource Efficiency**: Through load balancing, EdgeHive reduces the number of active application instances, optimizing resource use without sacrificing performance.

## 3. Simulation with PureEdgeSim

EdgeHive integrates with **PureEdgeSim**, a state-of-the-art edge computing simulator, to evaluate the effectiveness of its workload balancing mechanisms. The simulator allows the testing of various scenarios involving edge devices, user distribution, and application workload characteristics.

- **Real-World Datasets**: Simulations use datasets like the **Alibaba Trace Dataset**, which consists of microservices data and real-world user behavior, providing insights into how the system performs under different loads and configurations.

- **Experimental Setup**: The simulations involve 125 edge nodes deployed in a 1.2 km² area, with workloads derived from over 20,000 microservices, demonstrating the scalability of the system.

### Core Algorithms

#### Marginal Computing Cost per User (MCU):

MCU quantifies the residual capacity of an EDC to accept additional users. It represents the readiness of the system to accommodate new users based on its current load and resource availability.

The system’s performance is guided by the relationship between the average user cost and the marginal computing cost of a new user.

#### Collaborative Load Balancing Algorithm:

EDCs assess their own workload and the marginal cost of accommodating additional users. Based on this assessment, EDCs decide whether to offload users to neighboring edge nodes. The decision process is influenced by factors like latency, resource usage, and the willingness of neighboring EDCs to accept additional users.

## 5. User Migration:

Users can be migrated between edge devices to balance the load. This migration occurs based on the capacity of neighboring devices to handle the additional users while respecting latency constraints.

EdgeHive supports the partial migration of users, transferring subsets of users from one node to another instead of performing a complete migration. This helps reduce the system's migration overhead.

## 6. Simulation Results

EdgeHive's framework was evaluated through extensive experiments using different user distribution models (e.g., **Uniform Random** and **Random**) and various edge configurations. Key results include:

- **Load Balancing**: The system demonstrated improved load distribution across edge devices compared to baseline strategies. This is validated using **Lorenz curves** and **Gini coefficients** to measure inequality in resource distribution.

- **Impact of User Migrations**: The system's ability to efficiently migrate users was analyzed. As expected, higher values of MCU resulted in fewer migrations, contributing to system stability and performance.

- **Latency Management**: EdgeHive kept the latency within acceptable bounds by selectively migrating users based on their current load and the MCU cost, ensuring that migrations did not introduce excessive delays.

## 7. Conclusion

EdgeHive provides a scalable and efficient solution for decentralized resource management in edge computing environments. By integrating a policy-driven marginal cost model, the tool ensures optimal workload distribution while adhering to latency constraints. Its implementation in PureEdgeSim highlights its effectiveness in improving resource utilization and reducing the operational cost of edge systems.

EdgeHive’s approach is adaptable to real-world applications, particularly in environments with high user demands, such as **autonomous vehicles**, **healthcare**, and **industrial IoT**. Future improvements will focus on incorporating more dynamic models and real-time adaptations for user mobility and resource allocation.

## 8. PureEdgeSim Architecture

PureEdgeSim enables the simulation of resource management strategies and allows the evaluation of the performance of Cloud, Edge, and Mist computing environments. It guarantees high scalability by enabling the simulation of thousands of devices. Besides, it supports the heterogeneity of Edge devices (i.e., whether this device is mobile or not, whether battery-powered or not, different application requirements: tasks file size, tasks CPU utilization, and latency requirement, etc.).

![Environment](https://github.com/CharafeddineMechalikh/PureEdgeSim/blob/master/PureEdgeSim/files/scenario.JPG)

*Simple representation of the simulation scenarios.*

It provides a **task orchestrator module** that orchestrates the tasks and enables multi-tier simulation scenarios where many computing paradigms can be used in conjunction with one another. Besides, it provides a more realistic **network model** that continuously changes the allocated bandwidth for each task being transferred depending on network traffic.

It consists of the following **7 modules**:

- **Scenario Manager**: Loads the simulation parameters and user scenarios from input files (`.xml` and `.prop` files in `/settings/` folder).
  
- **Simulation Manager**: Initiates the simulation environment, schedules events, and generates the output.

- **Data Centers Manager**: Generates and manages all the data centers and devices (Cloud, Edge, or Mist).

- **Tasks Generator**: Behind the tasks generation, assigning applications to Edge devices and generating tasks based on the application type.

- **Network Module**: Manages the transfer of tasks/containers.

- **Tasks Orchestrator**: The decision-maker for task orchestration algorithms.

- **Location Manager**: Manages the mobility paths of mobile devices.

---

### References

Carlini, E., Dazzi, P., Ferrucci, L., Massa, J., Mordacchini, M.: **Marginal Cost of Computation as a Collaborative Strategy for Resource Management at the Edge** The 2024 International Conference on the Economics of Grids, Clouds, Systems, and Services, Rome, Italy

Ferrucci, L., Mordacchini, M.,  Dazzi, P.: **Decentralized Replica Management in Latency-Bound Edge Environments for Resource Usage Minimization** IEEE Access vol. 12, 2024

Ferrucci, L., Mordacchini, M., Coppola, M., Carlini, E., Kavalionak, H., Dazzi, P.: **Latency preserving self-optimizing placement at the edge** The 2020 1st FRAME Workshop, colocated with HPDC 2020, virtual event

Mechalikh, C., Taktak, H., Moussa, F.: **PureEdgeSim: A Simulation Toolkit for Performance Evaluation of Cloud, Fog, and Pure Edge Computing Environments**. The 2019 International Conference on High Performance Computing & Simulation (2019), 700-707.

