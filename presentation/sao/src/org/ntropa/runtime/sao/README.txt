Server Active Objects
An introductions to the basic concepts
Author: Janek D. Bogucki
Created: 2002-Jan-31
$Id: README.txt,v 1.2 2002/02/01 15:23:23 jdb Exp $

Section 1.
    Overview
    Control and render phases

Overview
--------

The server active framework (SAF) works with trees of three basic types:

    Fragments
    AbstractElements
    AbstractServerActiveObjects

AbstractElements can contain Fragments and AbstractServerActiveObjects.

AbstractServerActiveObjects can contain AbstractElements

Fragments can not contain any tyoe of object. They are an atomic component with
is used for blocks of HTML.

To be continued...


Control and render phases
-------------------------

When a WPS created JSP is served the root level SAO is asked to execute any control
logic. If no HTTP redirection has been requested then the root level SAO is asked to
execute the render phase. This mechanism supports the popular Model-View-Controller
architecture (MVC) while allowing the convenience of coding the control logic and
view rendering code in the same class(es).

This is the code from a WPS managed JSP

        component_1.control ( invocationBean ) ;
        
        if ( ib.getController ().proceed () ) {
            invocationBean.enableJspWriter () ;
            component_1.render ( invocationBean ) ;
        }

During the control method invocation the sao can invoke Controller.sendRedirect ( String location )
to indicate the page flow is changing. No HTML can be written to the buffer during this phase. If
this object completes it's control logic without requesting a page redirection then it should invoke
the control method of each child.

If a page redirection has been requested then the convention is to abandon all further control logic
contained in child saos. This conditional abandonment is tested for by invoking Controller.proceed ().

Note: invoking HttpServletResponse.sendRedirect ( ... ) directly breaks this functionality. Make sure
to invoke Controller.sendRedirect ( ... ) which then invokes HttpServletResponse.sendRedirect ( ... ).

(If there are other page flow control methods in HttpServletResponse we need to use we need to extend the
Controller interface to allow them.)

The implementation of control in AbstractServerActiveObject breaks the invocation into two other methods

