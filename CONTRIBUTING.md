# Contributing to SolarDroid

First off, thanks for taking the time to contribute! 🎉

The following is a set of guidelines for contributing to SolarDroid. These are mostly guidelines, not rules. Use your best judgment, and feel free to propose changes to this document in a pull request.

---

## Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code.

**Summary:**
- Use welcoming and inclusive language
- Be respectful of differing opinions and experiences
- Accept constructive criticism gracefully
- Focus on what is best for the community
- Show empathy towards other community members

---

## I Don't Want to Read This Whole Thing; I Just Have a Question

> **Note:** Please don't file an issue to ask a question. Use discussions instead.

- 💬 Ask in [GitHub Discussions](https://github.com/Nandazdev2/SolarDroid/discussions)
- 🌐 Visit [Solar2D Community Forum](https://solar2d.com/forums/)
- 📚 Check [Documentation](https://docs.solar2d.com/)

---

## How Can I Contribute?

### 🐛 Reporting Bugs

Before creating bug reports, please check the issue list as you might find out that you don't need to create one. When you are creating a bug report, please include as many details as possible:

**What to include in a bug report:**
- **Title:** Clear and descriptive
- **Description:** Detailed explanation of the bug
- **Steps to Reproduce:** Exact steps to reproduce the issue
- **Expected Behavior:** What you expected to happen
- **Actual Behavior:** What actually happened
- **Screenshots/Videos:** If applicable
- **Environment:**
  - Android version
  - Device model
  - SolarDroid version
  - Java version

**Example:**
```
Title: App crashes when opening large files

Description: SolarDroid crashes when trying to open Lua files larger than 5MB.

Steps to Reproduce:
1. Create a Lua file larger than 5MB
2. Open it in SolarDroid editor
3. Observe the crash

Expected: File should load
Actual: App crashes with NullPointerException

Device: Samsung Galaxy S21, Android 12
```

### 💡 Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, please include:

- **Title:** Clear and descriptive title
- **Description:** Clear description of the enhancement
- **Rationale:** Why this enhancement would be useful
- **Examples:** Examples of how it would work
- **Related Issues:** Any related issues

**Example:**
```
Title: Add dark theme support

Description: Add a dark theme option to reduce eye strain during night coding sessions.

Rationale: Many IDEs offer dark themes. It would improve usability for evening development.

Examples:
- Toggle in settings
- Auto-detection based on system theme
- Custom color schemes
```

### 🔧 Contributing Code

#### Getting the Code

1. **Fork the repository**
   ```bash
   # Go to https://github.com/Nandazdev2/SolarDroid
   # Click "Fork" button
   ```

2. **Clone your fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/SolarDroid.git
   cd SolarDroid
   ```

3. **Add upstream remote**
   ```bash
   git remote add upstream https://github.com/Nandazdev2/SolarDroid.git
   ```

#### Making Changes

1. **Create a new branch**
   ```bash
   git checkout -b feature/your-feature-name
   # or for bug fixes:
   git checkout -b fix/bug-description
   ```

2. **Make your changes**
   - Write clean, readable code
   - Add comments for complex logic
   - Follow the existing code style

3. **Test your changes**
   ```bash
   ./gradlew build
   ./gradlew installDebug
   ```

4. **Commit your changes**
   ```bash
   git add .
   git commit -m "Describe your changes clearly"
   ```

   **Commit message format:**
   ```
   Short summary (50 chars max)

   Detailed explanation of what changed and why.
   Include any relevant issue numbers: #123

   Type: feature/bugfix/docs/refactor
   ```

5. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Create a Pull Request**
   - Go to the original repository
   - Click "New Pull Request"
   - Select your branch
   - Fill in the PR template
   - Submit!

#### Pull Request Guidelines

- **Title:** Clear and descriptive
- **Description:** Explain the changes and why
- **References:** Link to related issues (#123)
- **Checklist:**
  - [ ] Code follows the style guidelines
  - [ ] Self-review completed
  - [ ] Comments added for complex logic
  - [ ] Documentation updated
  - [ ] No new warnings generated
  - [ ] Tests pass

**Example PR:**
```markdown
## Description
Fixes the crash when opening large Lua files.

## Related Issues
Closes #42

## Changes Made
- Added file size validation
- Implemented chunked file reading
- Added error handling for memory issues

## How to Test
1. Create a file > 5MB
2. Open it in editor
3. Verify it loads without crashing

## Checklist
- [x] Code follows style guidelines
- [x] Self-review completed
- [x] Tests pass locally
```

---

## Development Setup

### Prerequisites

- Java Development Kit (JDK) 8+
- Android Studio
- Android SDK (API 21-34)
- Gradle

### Build Commands

```bash
# Clone repository
git clone https://github.com/Nandazdev2/SolarDroid.git
cd SolarDroid

# Build the project
./gradlew build

# Install on device/emulator
./gradlew installDebug

# Run tests
./gradlew test

# Clean build
./gradlew clean build
```

### Project Structure

```
SolarDroid/
├── src/
│   └── com/
│       ├── ansca/              # Solar2D runtime
│       └── mkapp/              # SolarDroid IDE
├── res/                        # Android resources
├── libs/                       # External libraries
└── build.gradle               # Build configuration
```

---

## Styleguides

### Java Code Style

- **Indentation:** 4 spaces
- **Line Length:** Max 120 characters
- **Naming:**
  - Classes: `PascalCase` (e.g., `EditorActivity`)
  - Methods: `camelCase` (e.g., `openFile()`)
  - Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_FILE_SIZE`)
  - Variables: `camelCase` (e.g., `fileContent`)

**Example:**
```java
public class EditorActivity extends AppCompatActivity {
    private static final int MAX_FILE_SIZE = 5_000_000;
    
    private String fileContent;
    private TextView statusView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        initializeViews();
    }
    
    private void initializeViews() {
        statusView = findViewById(R.id.status_view);
    }
}
```

### Lua Code Style

- **Indentation:** 2 or 4 spaces (consistent)
- **Naming:**
  - Functions: `camelCase` (e.g., `createCircle()`)
  - Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_WIDTH`)
  - Local variables: `camelCase`

**Example:**
```lua
local display = require("display")
local MAX_RADIUS = 100

function createCircle(x, y, radius)
    local circle = display.newCircle(x, y, radius)
    circle:setFillColor(1, 0, 0)
    return circle
end
```

### Commit Messages

- Use the imperative mood ("add feature" not "added feature")
- Limit first line to 50 characters
- Reference issues and pull requests liberally
- Include a detailed explanation in the body

**Good examples:**
```
Add syntax highlighting to editor

Implement Lua keyword highlighting in the code editor.
Uses a custom lexer to identify keywords and operators.

Closes #123
```

---

## Additional Notes

### Issue Labels

- `bug` — Something isn't working
- `enhancement` — New feature or request
- `documentation` — Improvements to documentation
- `good first issue` — Good for newcomers
- `help wanted` — Extra attention is needed
- `in progress` — Someone is working on this
- `blocked` — Waiting for something else

### Pull Request Labels

- `pending review` — Waiting for review
- `changes requested` — Feedback given
- `approved` — Ready to merge
- `wip` — Work in progress

---

## Community

- 💬 [GitHub Discussions](https://github.com/Nandazdev2/SolarDroid/discussions)
- 🌐 [Solar2D Forums](https://solar2d.com/forums/)
- 📚 [Documentation](https://docs.solar2d.com/)

---

## Questions?

- 📖 Check the [README](README.md)
- 🔍 Search [existing issues](https://github.com/Nandazdev2/SolarDroid/issues)
- 💬 Ask in [Discussions](https://github.com/Nandazdev2/SolarDroid/discussions)

---

<div align="center">

**Thank you for contributing! 🙌**

Your efforts help make SolarDroid better for everyone.

</div>
