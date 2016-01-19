grammar JourneyCondition;

condition
  : expr rel_op expr
  ;

rel_op
  : '<' | '<=' | '>' | '>=' | '=~' | '!~' | '==' | '!=' | '<>' | '=' | K_INCLUDES | K_EXCLUDES
  ;

expr
 : literal_value
 | ( K_USER '.' )? field_name
 | expr arithmetic_op2 expr
 | expr arithmetic_op1 expr
 ;

arithmetic_op1
  : '+'
  | '-'
  ;

arithmetic_op2
  : '*'
  | '/'
  | '%'
  ;


field_name
 : any_name
 ;

any_name
 : IDENTIFIER
 | STRING_LITERAL
 | '(' any_name ')'
 ;

literal_value
 : INT_LITERAL
 | FLOAT_LITERAL
 | STRING_LITERAL
 ;

K_USER : U S E R;
K_INCLUDES : I N C L U D E S;
K_EXCLUDES : E X C L U D E S;

IDENTIFIER
 : '`' (~'`' | '``')* '`'
 | [a-zA-Z_] [a-zA-Z_\-0-9]* // TODO check: needs more chars in set
 ;

STRING_LITERAL
  : '\'' ( ~'\'' | '\'\'' )* '\''
  ;

INT_LITERAL
 : DIGIT+
 ;

FLOAT_LITERAL
 : DIGIT+ ( '.' DIGIT* )? ( E [-+]? DIGIT+ )?
 | '.' DIGIT+ ( E [-+]? DIGIT+ )?
 ;

SPACES
 : [ \u000B\t\r\n] -> channel(HIDDEN)
 ;

fragment DIGIT : [0-9];
fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];