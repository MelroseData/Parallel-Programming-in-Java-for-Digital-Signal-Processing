This project presents a Java-based benchmarking framework designed to evaluate the impact of multithreading on the performance of DSP cascade and series filters. The framework focuses on analyzing the trade-offs between thread management overhead and computational workload, providing quantitative insights into when parallelization is beneficial.

⚙️ Technical Highlights

Designed and implemented a Java benchmarking framework to evaluate multithreading performance on DSP cascade and series filters

Managed multithreaded execution using Java’s ExecutorService, enabling controlled task scheduling and concurrency

Systematically analyzed the trade-off between thread overhead and computation load

Achieved approximately 35% speedup for cascade filters

Execution time reduced from 21.2 ms to 14.6 ms

Quantified performance degradation in series filters, where fine-grained task decomposition caused thread management overhead to outweigh parallel gains




This project was originally completed as part of an academic assignment with a fixed submission deadline.
The GitHub repository was created after project completion for documentation and portfolio purposes.
Supporting evidence:

<img width="1760" height="972" alt="Screenshot 2026-01-06 194638" src="https://github.com/user-attachments/assets/89d2c5c6-2390-4931-9c76-4052c3a75b35" />


