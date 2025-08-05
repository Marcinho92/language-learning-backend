# AI Prompts Configuration

## Przegląd

Prompty używane przez `AiGrammarValidationService` zostały przeniesione do pliku konfiguracyjnego `src/main/resources/ai-prompts.yml`. To pozwala na łatwe przeglądanie i modyfikowanie treści promptów bez konieczności edycji kodu Java.

## Struktura pliku konfiguracyjnego

Plik `ai-prompts.yml` zawiera dwie główne sekcje:

### 1. Main Prompt (`main-prompt`)
Główny prompt używany do walidacji zdań. Zawiera:
- Instrukcje dla AI
- Format odpowiedzi JSON
- Reguły walidacji
- Listę tematów gramatycznych i ich reguł

### 2. Grammar Explanations (`grammar-explanations`)
Szczegółowe wyjaśnienia dla każdego tematu gramatycznego, zawierające:
- Opis użycia
- Strukturę gramatyczną
- Przykłady zdań

## Jak modyfikować prompty

### Modyfikacja głównego promptu
Edytuj sekcję `main-prompt` w pliku `ai-prompts.yml`:

```yaml
ai:
  grammar:
    validation:
      main-prompt: |
        Twoja nowa treść promptu tutaj...
        Użyj %s jako placeholder dla:
        - %s - zdanie użytkownika
        - %s - oryginalne słowo
        - %s - tłumaczenie słowa
        - %s - temat gramatyczny
```

### Modyfikacja wyjaśnień gramatycznych
Edytuj sekcję `grammar-explanations`:

```yaml
ai:
  grammar:
    validation:
      grammar-explanations:
        present-simple: |
          Twoje nowe wyjaśnienie Present Simple...
        
        # Dodaj nowy temat
        nowy-temat: |
          Wyjaśnienie nowego tematu...
```

### Dodawanie nowych tematów gramatycznych
1. Dodaj regułę do sekcji `main-prompt`
2. Dodaj wyjaśnienie do sekcji `grammar-explanations`
3. Użyj kebab-case (myślniki zamiast spacji) jako klucz

## Mapowanie nazw tematów

Nazwy tematów są automatycznie konwertowane:
- "Present Simple" → "present-simple"
- "Modal Verbs" → "modal-verbs"
- itp.

## Restart aplikacji

Po modyfikacji pliku konfiguracyjnego należy zrestartować aplikację, aby zmiany zostały załadowane.

## Przykład modyfikacji

Aby dodać nowy temat "Present Perfect Continuous":

1. Dodaj do sekcji `main-prompt`:
```yaml
- Present Perfect Continuous: Subject + have/has + been + verb + ing
```

2. Dodaj do sekcji `grammar-explanations`:
```yaml
present-perfect-continuous: |
  Present Perfect Continuous is used for actions that started in the past and continue to the present.
  
  Structure: Subject + have/has + been + verb + ing
  Examples:
  • I have been working here for 5 years.
  • She has been studying all night.
  • They have been waiting for hours.
```

## Korzyści z tego rozwiązania

1. **Łatwość modyfikacji** - prompty można edytować bez znajomości Java
2. **Przejrzystość** - wszystkie prompty w jednym miejscu
3. **Wersjonowanie** - zmiany w promptach są śledzone w Git
4. **Separacja odpowiedzialności** - logika biznesowa oddzielona od treści
5. **Hot reload** - zmiany można wprowadzać bez rekompilacji (po restarcie) 