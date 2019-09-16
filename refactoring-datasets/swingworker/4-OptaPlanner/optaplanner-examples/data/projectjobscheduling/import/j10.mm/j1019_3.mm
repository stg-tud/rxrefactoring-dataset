************************************************************************
file with basedata            : mm19_.bas
initial value random generator: 1036933791
************************************************************************
projects                      :  1
jobs (incl. supersource/sink ):  12
horizon                       :  78
RESOURCES
  - renewable                 :  2   R
  - nonrenewable              :  2   N
  - doubly constrained        :  0   D
************************************************************************
PROJECT INFORMATION:
pronr.  #jobs rel.date duedate tardcost  MPM-Time
    1     10      0       16        2       16
************************************************************************
PRECEDENCE RELATIONS:
jobnr.    #modes  #successors   successors
   1        1          3           2   3   4
   2        3          1          10
   3        3          3           5   6   8
   4        3          1           5
   5        3          1           9
   6        3          1           7
   7        3          2          10  11
   8        3          3           9  10  11
   9        3          1          12
  10        3          1          12
  11        3          1          12
  12        1          0        
************************************************************************
REQUESTS/DURATIONS:
jobnr. mode duration  R 1  R 2  N 1  N 2
------------------------------------------------------------------------
  1      1     0       0    0    0    0
  2      1     1       0    5    9    0
         2     4       0    3    0    4
         3     5       0    3    8    0
  3      1     6       0    5    3    0
         2     7       2    0    0    5
         3     8       0    4    0    4
  4      1     2       0    3    3    0
         2     5       0    3    0    2
         3     6       6    0    1    0
  5      1     2       9    0    7    0
         2     2       0    5    7    0
         3     8       0    4    7    0
  6      1     6       1    0    8    0
         2     8       0    4    6    0
         3    10       0    3    3    0
  7      1     2       6    0    8    0
         2     4       4    0    0    6
         3     6       0    5    3    0
  8      1     2       2    0    0    7
         2     7       0    4    8    0
         3     8       0    3    6    0
  9      1     5       0    6    0    7
         2     6       0    6    7    0
         3     8       1    0    0    5
 10      1     2       5    0    0    9
         2     2       8    0    5    0
         3     9       0    3    0    9
 11      1     2       0    9    3    0
         2     7       0    8    0    5
         3    10       1    0    0    1
 12      1     0       0    0    0    0
************************************************************************
RESOURCEAVAILABILITIES:
  R 1  R 2  N 1  N 2
    8   10   46   32
************************************************************************
