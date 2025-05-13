### 1. synchronized keyword on method - Monitor
```
    class ClassWithCriticalSections {
        public synchronized void method1(){
            ...Thread A
        }

        public synchronized void method2(){
            ...
        }
    }
```

    If thread A is executed on method1, threadB can't enter into any sync methods
    of this class as the lock is obtained at class level.

### 2. synchronized keyword on blocks - Lock (It's a reentrant).
```
    class ClassWithCriticalSections {

        Object lockingObject = new Object();

        public void method1(){
            synchronized (lockingObject) {
                Critical Section
                ...
            }
        }

        public void method2(){
            synchronized (lockingObject) {
                Critical Section
                ...
            }
        }
    }
```
    Any sync block which is synchronized on same object will only one thread to execute
    at any time. Other threads will be in wait state until the lock is released.
    Provides a flexibility of using any object for synchronization.
    Example: 2 threads can  execute method1 and method2 concurrently as they are using diff locks.
```
        class ClassWithCriticalSections {
            Object lock1 = new Object();
            Object lock2 = new Object();

            public void method1(){
                synchronized (lock1) {
                    Critical Section
                    ...
                }
            }

            public void method2(){
                synchronized (lock2) {
                    Critical Section
                    ...
                }
            }
        }
 */
```