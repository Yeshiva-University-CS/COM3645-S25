## **Emmy Language Summary**

### **Data Types**
- `integer`: Whole numbers (e.g., 5)
- `real`: Decimal numbers (e.g., 3.14)
- `boolean`: `true` or `false`
- `string`: Quoted strings (e.g., `"hello"`)
- `none`: Null-like value

### **Declarations**
- **Variables**: `var x = 5;`
- **Functions**: `let add a b = a + b;` or with a block body:

  ```emmy
  let greet name = {
    var message = "Hello, " + name;
    return message;
  }
  ```

### **Expressions and Operators**

| Type             | Operators                     | Example                    |
|------------------|-------------------------------|----------------------------|
| Arithmetic       | `+`, `-`, `*`, `/`, unary `-` | `x + y`, `-a`, `a / b`     |
| String Concatenation | `+`                          | `"Hi, " + name`            |
| Logical          | `and`, `or`, `!`               | `a and b`, `!flag`         |
| Comparison       | `<`, `>`, `<=`, `>=`           | `x < y`, `score >= 90`     |
| Equality         | `==`, `!=`                     | `x == y`, `flag != true`   |
| Assignment       | `=`                            | `x = y + 1;`               |
| Function call    | `f(x, y)`                      | `add(2, 3)`                |

### **Statements and Control Flow**
- **Conditionals**: `if (cond) { ... } else { ... }`
- **Loops**:
  - `while (cond) { ... }`
  - `loop { ... } until (cond);`
  - `repeat n times { ... }`
- **Return**: `return value;` (only inside functions)
- **Blocks**: `{ ... }` introduce a new scope
