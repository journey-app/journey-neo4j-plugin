grammar SelectStatement;

statement: tuple transform*;
tuple: branch
    | LEFT_PARENT branch (COMMA branch)* RIGHT_PARNT;

branch: collector | collector transform*;
transform: PIPE_FORWARD transformFN;
collector: NAME;
transformFN: NAME;


LEFT_PARENT: '(';
RIGHT_PARNT: ')';
NAME: [a-zA-Z0-9_\-.:\/]+;
PIPE_FORWARD: '|>';
COMMA: ',';
WS: [ \t\r\n]+ -> skip;
