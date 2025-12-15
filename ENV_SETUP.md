# Environment Configuration Setup

## Quick Start

1. **Copy the example file:**
   ```bash
   cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
   ```

2. **Edit `application-local.yml` with your actual API keys:**
   ```yaml
   api:
     openai:
       key: sk-proj-your-actual-openai-key
     usda:
       key: your-actual-usda-key
   ```

3. **Run the backend:**
   ```bash
   ./start-backend.sh
   ```

## How It Works

- **`application.yml`**: Base configuration (committed to git)
- **`application-local.yml`**: Local overrides with API keys (gitignored)
- **Profile**: The app runs with `local` profile, which loads `application-local.yml`

## Alternative: Environment Variables

You can also use environment variables (they take precedence):

```bash
export OPENAI_API_KEY=your-key
export USDA_API_KEY=your-key
./start-backend.sh
```

## Files Structure

```
src/main/resources/
├── application.yml              # Base config (safe to commit)
├── application-local.yml        # Your actual keys (gitignored)
└── application-local.yml.example # Template (committed)
```

## What's Gitignored

- `application-local.yml` - Your actual API keys
- `.env` - Environment variables file

These files are in `.gitignore` and will never be committed to the repository.
