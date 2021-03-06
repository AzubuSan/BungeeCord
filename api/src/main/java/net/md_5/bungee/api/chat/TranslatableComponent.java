package net.md_5.bungee.api.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@NoArgsConstructor
public class TranslatableComponent extends BaseComponent
{
    private final ResourceBundle locales = ResourceBundle.getBundle( "mojang-translations/en_US" );
    private final Pattern format = Pattern.compile( "%(?:(\\d+)\\$)?([A-Za-z%]|$)" );

    /**
     * The key into the Minecraft locale files to use for the
     * translation. The text depends on the client's locale setting.
     * The console is always en_US
     */
    private String translate;
    /**
     * The components to substitute into the translation
     */
    private List<BaseComponent> with;

    /**
     * Creates a translatable component with the passed substitutions
     * @see #setTranslate(String)
     * @see #setWith(java.util.List)
     * @param translate the translation key
     * @param with the {@link java.lang.String}s and {@link net.md_5.bungee.api.chat.BaseComponent}s
     *             to use into the translation
     */
    public TranslatableComponent(String translate, Object... with)
    {
        setTranslate( translate );
        List<BaseComponent> temp = new ArrayList<>();
        for ( Object w : with )
        {
            if ( w instanceof String )
            {
                temp.add( new TextComponent( (String) w ) );
            } else
            {
                temp.add( (BaseComponent) w );
            }
        }
        setWith( temp );
    }

    /**
     * Sets the translation substitutions to be used in
     * this component. Removes any previously set
     * substitutions
     * @param components the components to substitute
     */
    public void setWith(List<BaseComponent> components)
    {
        for ( BaseComponent component : components )
        {
            component.parent = this;
        }
        with = components;
    }

    /**
     * Adds a text substitution to the component. The text will
     * inherit this component's formatting
     *
     * @param text the text to substitute
     */
    public void addWith(String text)
    {
        addWith( new TextComponent( text ) );
    }

    /**
     * Adds a component substitution to the component. The text will
     * inherit this component's formatting
     *
     * @param component the component to substitute
     */
    public void addWith(BaseComponent component)
    {
        if ( with == null )
        {
            with = new ArrayList<>();
        }
        component.parent = this;
        with.add( component );
    }

    @Override
    protected void toPlainText(StringBuilder builder)
    {
        String trans = locales.getString( translate );
        if ( trans == null )
        {
            builder.append( translate );
        } else
        {
            Matcher matcher = format.matcher( trans );
            int position = 0;
            int i = 0;
            while ( matcher.find( position ) )
            {
                int pos = matcher.start();
                if ( pos != position )
                {
                    builder.append( trans.substring( position, pos ) );
                }
                position = matcher.end();

                String formatCode = matcher.group( 2 );
                switch ( formatCode.charAt( 0 ) )
                {
                    case 's':
                    case 'd':
                        String withIndex = matcher.group( 1 );
                        with.get( withIndex != null ? Integer.parseInt( withIndex ) - 1 : i++ ).toPlainText( builder );
                        break;
                    case '%':
                        builder.append( '%' );
                        break;
                }
            }
            if ( trans.length() != position )
            {
                builder.append( trans.substring( position, trans.length() ) );
            }
        }
        super.toPlainText( builder );
    }

    @Override
    protected void toLegacyText(StringBuilder builder)
    {
        String trans = locales.getString( translate );
        if ( trans == null )
        {
            addFormat( builder );
            builder.append( translate );
        } else
        {
            Matcher matcher = format.matcher( trans );
            int position = 0;
            int i = 0;
            while ( matcher.find( position ) )
            {
                int pos = matcher.start();
                if ( pos != position )
                {
                    addFormat( builder );
                    builder.append( trans.substring( position, pos ) );
                }
                position = matcher.end();

                String formatCode = matcher.group( 2 );
                switch ( formatCode.charAt( 0 ) )
                {
                    case 's':
                    case 'd':
                        String withIndex = matcher.group( 1 );
                        with.get( withIndex != null ? Integer.parseInt( withIndex ) - 1 : i++ ).toLegacyText( builder );
                        break;
                    case '%':
                        addFormat( builder );
                        builder.append( '%' );
                        break;
                }
            }
            if ( trans.length() != position )
            {
                addFormat( builder );
                builder.append( trans.substring( position, trans.length() ) );
            }
        }
        super.toLegacyText( builder );
    }

    private void addFormat(StringBuilder builder)
    {
        builder.append( getColor() );
        if ( isBold() ) builder.append( ChatColor.BOLD );
        if ( isItalic() ) builder.append( ChatColor.ITALIC );
        if ( isUnderlined() ) builder.append( ChatColor.UNDERLINE );
        if ( isStrikethrough() ) builder.append( ChatColor.STRIKETHROUGH );
        if ( isObfuscated() ) builder.append( ChatColor.MAGIC );
    }

    @Override
    public String toString()
    {
        return String.format( "TranslatableComponent{translate=%s, with=%s, %s}", translate, with, super.toString() );
    }
}
