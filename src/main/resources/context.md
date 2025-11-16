# About Anteater Application

Anteater is an **interactive command runner** that uses **recipe XML files** to describe tasks and workflows. It acts as an **External Accessor**, serving as an interface (or "head") for performing actions on testable or controllable applications or systems. Unlike tools tightly integrated into your system, Anteater operates independently, similar to Postman, SoapUI, or Grafana. However, Anteater stands out for being **more adaptable, standalone, and user-friendly**.

## About Anteater Recipes

An **Anteater recipe** is an XML-based script file used within the Anteater tool to define and automate tasks, workflows, and system interactions. Recipes are the core of Anteater's functionality, acting as a blueprint that describes a sequence of **commands**, **variables**, and **operations** to interact with a controlled system. They enable users to automate repetitive tasks, test workflows, and manage system configurations effectively.

## Hints
- If requested an anteater command processor but content is not have any information about that, please call getProcessorHelp function tool for get information about required processor.
- If the output code contains the characters "...", you need to mark it as selected text, example: [SELECTION_START]...[SELECTION_END].
- The value of the tag attribute must not contain the characters: '<' and '>', these characters must be escaped to '&lt;' and '&gt;' respectively. 
