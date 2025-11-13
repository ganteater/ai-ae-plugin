# Commands

Note: All command tag and attribute names are case sensitive, please use them as shown in the examples.

## **Information Commands*

### **About**

The `<About>` tag is used to provide metadata and descriptive information about a recipe or module in Anteater. It allows you to document details such as the purpose, author, attachments, and additional context for the recipe. This tag enhances the readability and maintainability of recipes by ensuring important information is easily accessible.

#### **Structure and Usage**

The `<About>` tag can contain the following nested elements:

1. **`<description>`**:
   - Provides a textual description of the recipe or module.
   - Use this to explain the purpose, functionality, or context of the recipe.
   - Example:
     ```xml
     <About>
         <description>This recipe is used for validating API responses and ensuring data integrity.</description>
     </About>
     ```

2. **`<author>`**:
   - Specifies the author of the recipe, along with their contact information.
   - Attributes:
     - `name`: Name of the author.
     - `email`: Email address for communication.
     - `messager`: Messenger handle (e.g., Slack, Skype).
     - `phone`: Phone number for direct contact.
   - Example:
     ```xml
     <About>
         <author name="John Doe" email="john.doe@example.com" messager="Slack: @johndoe" phone="+1234567890" />
     </About>
     ```

3. **`<attach>`**:
   - Used to attach files or resources related to the recipe.
   - Attributes:
     - `file`: Path or URL to the attached file.
     - `width`: Width of the attachment (optional, for visual elements).
     - `height`: Height of the attachment (optional, for visual elements).
   - Example:
     ```xml
     <About>
         <attach>
             <file name="https://example.com/documentation.pdf" />
         </attach>
     </About>
     ```

4. **Combined Example**:
   - You can combine multiple elements within the `<About>` tag for more comprehensive documentation.
   - Example:
     ```xml
     <About>
         <description>This recipe automates user data validation and updates.</description>
         <author name="Jane Smith" email="jane.smith@example.com" messager="Skype: jane_smith" phone="+9876543210" />
         <attach>
             <file name="https://example.com/user-guide.pdf" width="800" height="600" />
         </attach>
     </About>
     ```

## Variable and Data Management

### **Var**
Defines variables of various types (e.g., text, number, array, JSON). Supports static values, user input, and dynamic initialization.

**Attributes**:
- `name`: Unique identifier for the variable.
- `type`: Data type (e.g., text, array, json, map).
- `value`: Literal value for the variable.
- `init`: Defines initialization behavior (`console`, `mandatory`).

**Example**:
```xml
<Var name="greeting" value="Hello, World!" />
<Out name="greeting" />
```
**Result**: Outputs `"Hello, World!"`.<Out>Welcome to Anteater!</Out>


### **Out**
Outputs data, variable values, or text. Can include attributes like type, level, or description.

**Attributes**:
- `name`: Variable to output.
- `type`: Format of the output (e.g., txt, json, xml).
- `level`: Logging level (`info`, `warn`, `error`, etc.).

**Example**:
```xml
<Var name="welcome" value="Welcome to Anteater" />
<Out name="welcome" type="txt" />
<Out level="warn">This is a warning message.</Out>
```
**Result**: Outputs `"Welcome to Anteater"` and `"This is a warning message."`.

### **Wait**
Pauses execution for a specified number of milliseconds.

**Attributes**:
- `delay`: Duration of the pause in milliseconds.

**Example**:
```xml
<Wait delay="5000" />
<Out>The recipe waited for 5 seconds.</Out>
```
**Result**: Pauses for 5 seconds before outputting the message.

### **If** and **Else**
Implements conditional logic in recipes. `<Else>` provides fallback logic when `<If>` conditions are not met.

**Attributes (for `<If>`)**:
- `value`: The value to compare.
- `equals`, `isNull`, `isNotNull`: Conditions to evaluate.
- `expression`: Advanced logic (e.g., `$var{x} > 10`).

**Example**:
```xml
<Var name="score" value="85" />
<If value="$var{score}" equals="100">
  <Out>Perfect Score!</Out>
  <Else>
    <Out>Not a perfect score.</Out>
  </Else>
</If>
```
**Result**: Outputs `"Not a perfect score."` because `score` is not equal to `100`.

### **While**
Executes a loop while a condition remains true. Useful for repetitive tasks.

**Attributes**:
- `name`: Variable that controls the loop.
- `notEqual`, `equals`: Conditions to evaluate.

**Example**:
```xml
<Var name="count" value="0" />
<While name="count" notEqual="5">
  <Out name="count" />
  <Inc name="count" />
</While>
<Out>Loop completed!</Out>
```
**Result**: Outputs `0`, `1`, `2`, `3`, `4`, and then `"Loop completed!"`.

### **Calculate**
Performs arithmetic and logical operations using an expression syntax (powered by JEXL).

**Attributes**:
- `name`: Variable to store the result.
- `expressions`: The arithmetic expression to evaluate.

**Example**:
```xml
<Var name="x" value="10" />
<Var name="y" value="5" />
<Calculate name="result" expressions="$var{x} + $var{y}" />
<Out name="result" />
```
**Result**: Outputs `15`.

### **Threads**
Executes multiple tasks in parallel threads for concurrency.

**Attributes**:
- `name`: The name of the defined task.
- `numbers`: Number of threads.
- `multi`: Set to `"true"` to enable parallelism.

**Example**:
```xml
<Threads name="task1" numbers="3" multi="true">
  <Out>Thread $var{THREAD_ID} is running...</Out>
</Threads>
<Out>All threads are complete.</Out>
```
**Result**: Runs 3 threads in parallel with unique `THREAD_ID`s and outputs a final message.

### **Get**
Performs an HTTP GET request to fetch data, storing the response in a variable.

**Attributes**:
- `name`: Variable to hold the HTTP response.
- `url`: The target URL for the GET request.

**Example**:
```xml
<Get name="response" url="https://api.example.com/data" />
<Out name="response" />
```

### **Append**
Adds elements to arrays or strings.

**Attributes**:
- `name`: Array or string to append to.
- `value`: Value to append.

**Example**:
```xml
<Var name="list" type="array" />
<Append name="list" value="Item 1" />
<Append name="list" value="Item 2" />
<Out name="list" />
```
**Result**: Outputs `["Item 1", "Item 2"]`.

### **Remove**
Deletes variables or specific keys.

**Attributes**:
- `name`: Variable or key to remove.
- `history`: Whether to retain historic data (`true` or `false`).

**Example**:
```xml
<Var name="example" value="data" />
<Out name="example" />
<Remove name="example" />
<Out name="example" />
```
**Result**: Outputs `data` first, then nothing after the variable is removed.

## Data Operations
Here are detailed examples for the **Data Operations** commands you requested:

### **ArrayElement**

The `<ArrayElement>` command allows you to retrieve specific elements from an array using their index position.

**Attributes:**
- `name`: The array variable to access.
- `index`: The index of the item to retrieve (0-based).

**Example:**
```xml
<Var name="myArray" type="array">
  <item>Item 1</item>
  <item>Item 2</item>
  <item>Item 3</item>
</Var>
<ArrayElement name="myArray" index="1" />
<Out name="myArray::1" />
```
**Result:** Outputs `"Item 2"` because the element at index `1` is retrieved.

### **ArraySize**

The `<ArraySize>` command calculates the number of items in an array and stores the result in a variable.

**Attributes:**
- `name`: The name of the array variable.
- `result`: The name of the variable to store the size of the array.

**Example:**
```xml
<Var name="myArray" type="array">
  <item>Item 1</item>
  <item>Item 2</item>
  <item>Item 3</item>
</Var>
<ArraySize name="myArray" result="arraySize" />
<Out name="arraySize" />
```
**Result:** Outputs `"3"` because the array contains three items.

### **CheckInArray**

The `<CheckInArray>` command checks if a specific value exists within an array. If the value exists, the execution continues; otherwise, an error message (optional) can be triggered.

**Attributes:**
- `name`: The name of the array variable.
- `value`: The value to check for existence in the array.
- `onErrorMsg`: Custom error message to display if the value is not found.

**Example:**
```xml
<Var name="myArray" type="array">
  <item>Item 1</item>
  <item>Item 2</item>
  <item>Item 3</item>
</Var>
<CheckInArray name="myArray" value="Item 2" onErrorMsg="Item not found in array!" />
<Out>Item exists!</Out>
```
**Result:** Outputs `"Item exists!"`, as `"Item 2"` exists in the array.

### **Listdir**

The `<Listdir>` command retrieves a list of files and directories within a specified directory path and stores them in an array variable.

**Attributes:**
- `name`: The variable to hold the result (array).
- `path`: Specifies the directory to list.
- `filter`: Optional filter to select specific file types.

**Example:**
```xml
<Listdir name="directoryContents" path="/path/to/directory" />
<Out name="directoryContents" />
```
**Result:**
Outputs an array of filenames and subdirectory names in `/path/to/directory`.

### **Replace**

The `<Replace>` command allows you to replace substrings within a variable or text.

**Attributes:**
- `name`: Name of the variable to manipulate.
- `search`: The string or pattern to find.
- `replace`: The replacement string.

**Example:**
```xml
<Var name="text" value="Hello, World!" />
<Replace name="text" search="World" replace="Anteater" />
<Out name="text" />
```
**Result:** Outputs `"Hello, Anteater!"` as `"World"` is replaced with `"Anteater"`.

### **Sort**

The `<Sort>` command sorts the elements of an array. By default, sorting is in ascending order, but custom attributes can define different sorting behaviors.

**Attributes:**
- `name`: The name of the array to sort.
- `order`: Sorting order (`asc` for ascending, `desc` for descending).

**Example:**
```xml
<Var name="myArray" type="array">
  <item>3</item>
  <item>1</item>
  <item>2</item>
</Var>
<Sort name="myArray" order="asc" />
<Out name="myArray" />
```
**Result:** Outputs `[1, 2, 3]`, as the array is sorted in ascending order.

**Example (Descending Order):**
```xml
<Sort name="myArray" order="desc" />
<Out name="myArray" />
```
**Result:** Outputs `[3, 2, 1]`.

### **Additional Notes**
These commands allow you to manipulate arrays, directories, and strings effectively. They are commonly used in workflows that require data validation, retrieval, manipulation, or sorting.

## **Interactive/Utility**
- **`Choice`**: Defines selectable tasks for branching logic.
- **`Confirm`**: Pauses execution until user confirmation.
- **`Load`**: Dynamically loads data, such as from files or input.
- **`Parameters`**: Defines or retrieves configuration parameters.
- **`Textparser`**: Parses raw text and extracts data.

## **Environment and External**
- **`Command`**: Executes shell or system commands.
- **`Extern`**: Executes external processes or scripts.
- **`NetworkInterfaces`**: Returns local IP or network resolution info.
- **`Server`**: Starts a simple server to handle requests.

## **Validation**
- **`CheckProperties`**: Validates properties of files or variables.
- **`Exist`**: Verifies the existence of variables or resources.

## **Advanced Operations**
- **`Regexp`**: Validates or transforms data with regular expressions.
- **`Xslt`**: Performs XSLT operations for XML data transformations.
- **`Time`**: Measures the duration of commands or tasks.
- **`Pragma`**: Sets execution rules or directives.

## **Loop/Complex Iteration**
- **`IterationRules`**: Processes loops with predefined rules.
- **`Rnd`**: Generates random values for use in recipes.
- **`WhileRun`**: Executes commands while displaying a "running" message.

