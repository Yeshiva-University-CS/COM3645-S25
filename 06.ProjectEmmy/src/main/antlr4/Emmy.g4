grammar Emmy;

@header {
    package antlr4;
    import edu.yu.compilers.intermediate.symtable.SymTableEntry;
    import edu.yu.compilers.intermediate.type.Typespec;
}

program : declaration* # programStart;

declaration
    : funcDecl  # declFunction
    | varDecl   # declVariable
    | statement # declStatement
    ;

funcDecl
    locals [SymTableEntry entry = null]
    : 'let' name=IDENTIFIER (params+=IDENTIFIER)* '=' functionBody # functionDeclaration;

functionBody
    : expression ';'   # expressionBody
    | block            # blockBody
    ;

varDecl 
    locals [SymTableEntry entry = null]
    : 'var' id=IDENTIFIER ('=' init=expression)? ';' # variableDeclaration;

// Statements

statement
    : exprStmt     # stmtExpression
    | printStmt    # stmtPrint
    | ifStmt       # stmtIf
    | returnStmt   # stmtReturn
    | whileStmt    # stmtWhile
    | untilStmt    # stmtUntil
    | repeatStmt   # stmtRepeat
    | block        # stmtBlock
    ;

exprStmt : expression ';' # expressionStatement;

printStmt : 'print' value=expression ';' # printStatement;

ifStmt : 'if' '(' condition=expression ')' thenBranch=statement
        ('else' elseBranch=statement)? # ifStatement;

returnStmt : 'return' value=expression? ';' # returnStatement;

whileStmt : 'while' '(' condition=expression ')' body=statement # whileStatement;

untilStmt : 'loop' body=statement 'until' '(' condition=expression ')'  # untilStatement;

repeatStmt : 'repeat' count=expression 'times' body=statement # repeatStatement;

block : '{' (declarations+=declaration)* '}' # blockStatement;

// Expressions

expression
    locals [Typespec type = null, Object value = null]    
    : assignment # expr;

assignment     
    locals [Typespec type = null, Object value = null]    
    : id=IDENTIFIER '=' rhs=assignment # assignmentExpr
    | logic_or                         # assignmentOr
    ;

logic_or     
    locals [Typespec type = null, Object value = null]     
    : left=logic_and (op+='or' right+=logic_and)* # logicalOr;

logic_and  
    locals [Typespec type = null, Object value = null]       
    : left=equality (op+='and' right+=equality)* # logicalAnd;

equality     
    locals [Typespec type = null, Object value = null]     
    : left=comparison (op+=('!=' | '==') right+=comparison)* # equalityExpr;

comparison 
    locals [Typespec type = null, Object value = null]       
    : left=term (op+=('>' | '>=' | '<' | '<=') right+=term)* # comparisonExpr;

term       
    locals [Typespec type = null, Object value = null]       
    : left=factor (op+=('+' | '-') right+=factor)* # termExpr;

factor     
    locals [Typespec type = null, Object value = null]       
    : left=unary (op+=('*' | '/') right+=unary)* # factorExpr;

unary      
    locals [Typespec type = null, Object value = null]       
    : op=('!' | '-') right=unary # unaryExpr
    | call                       # unaryCall
    ;

call       
    locals [Typespec type = null, Object value = null, SymTableEntry entry = null]  
    : primary ('(' args+=arguments? ')')* # callExpr;

primary 
    locals [Typespec type = null, Object value = null, SymTableEntry entry = null]    
    : 'true'                     # primaryTrue
    | 'false'                    # primaryFalse
    | 'none'                     # primaryNone
    | num=NUMBER                 # primaryNumber
    | str=STRING                 # primaryString
    | id=IDENTIFIER              # primaryIdentifier
    | '(' inner=expression ')'   # primaryParenthesis
    ;

arguments : first=expression (',' rest+=expression)* # argumentList;     

NUMBER     : DIGIT+ ('.' DIGIT+)? ;
STRING     : '"' ~('"')* '"' ;
IDENTIFIER : ALPHA (ALPHA | DIGIT)* ;
ALPHA      : [a-zA-Z_] ;
DIGIT      : [0-9] ;

COMMENT : '#' ~('\r' | '\n')* -> skip ;
WS      : [ \t\r\n]+ -> skip ;
