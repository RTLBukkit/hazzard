# Hazzard

A fork of http://www.github.com/Kyori/hazzard with comprehensible names.

![i made this](https://i.imgur.com/XhOmIUh.png)


![GitHub branch checks state](https://img.shields.io/github/checks-status/KyoriPowered/hazzard/main?style=flat-square)
![Codecov](https://img.shields.io/codecov/c/github/KyoriPowered/hazzard?style=flat-square)

> **Q:** What do you say when you're gonna drunk dial someone?
>
> **A:** `hazzardMessages.alCoholYou()`

A powerful localisation library to enforce [DRY] and simplify your code to the greatest extent allowed by your boss.

[DRY]: https://en.wikipedia.org/wiki/Don%27t_repeat_yourself

Hazzard is a localization library capable of creating proxy/mock classes, that implement an annotated interface.

# Basic Usage

It can inflate the following interface into an instance capable of returning enriched localizations 
or sending the message using a registered service.

```java
public interface MessageService {

	// Returns an enriched string for further composition, using "error.ticket_closed" as a lookup key that can define
    // a template, localization, resourcebundle property key, etc.
	@TranslationKey("error.ticket_closed")
	RichString exceptionTicketClosed();
	
	// Returns an enriched string for further composition, using the templateArgument sender.
    // e.g. say if RichString represented HTML, invoking it with exceptionInvalidSender("bob"); 
    // the returned literal could be a filled template of 
    // <span><strong>Error: </strong> invalid sender <pre>bob</pre></span>
	@TranslationKey("error.invalid_sender")
	RichString exceptionInvalidSender(@Placeholder String sender);

	
    // Sends the following viewer the message, with all placeholders substituted, via the registered messaging sending service.
	@TranslationKey("feedback.create")
	void feedbackCreate(Audience viewer, @Placeholder Ticket ticket);
}
```