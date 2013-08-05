package de.beyondjava.jsfComponents;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.primefaces.component.outputlabel.OutputLabel;

@FacesComponent("de.beyondjava.InputText")
public class NGInputText extends org.primefaces.component.inputtext.InputText implements SystemEventListener
{
   public static final String COMPONENT_FAMILY = "javax.faces.Input";

   public String getFamily()
   {
      return COMPONENT_FAMILY;
   }

   public NGInputText()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      UIViewRoot root = context.getViewRoot();
      root.subscribeToViewEvent(PreRenderViewEvent.class, this);
   }

   public String getClientId(FacesContext context)
   {
      try
      {
         String id = ELTools.getNGModel(this);
         return id;
      }
      catch (IOException p_error)
      {
         System.err.println("An error occurred: " + p_error);
         p_error.printStackTrace();
         return null;
      }
   }

   /**
    * if no label is provided by the XHTML file, try to guess it from the
    * ng-model attribute.
    */
   @Override
   public String getLabel()
   {
      String label = super.getLabel();
      if (null == label)
      {
         String ngModel;
         try
         {
            ngModel = ELTools.getNGModel(this);
            return NGWordUtiltites.labelFromCamelCase(ngModel);
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
      return label;
   }

   @Override
   public void encodeBegin(FacesContext context) throws IOException
   {
      if ("text".equals(getType()))
      {
         Class<?> type = ELTools.getType(this);
         if (int.class == type || Integer.class == type || long.class == type || Long.class == type
               || double.class == type || Double.class == type)
         {
            setType("number");
         }
      }
      super.encodeBegin(context);
   }

   @Override
   public boolean isRequired()
   {
      Annotation[] annotations = ELTools.readAnnotations(this);
      for (Annotation a : annotations)
      {
         if (a instanceof NotNull)
            return true;
      }
      return super.isRequired();
   }

   @Override
   public int getMaxlength()
   {
      Annotation[] annotations = ELTools.readAnnotations(this);
      for (Annotation a : annotations)
      {
         if (a instanceof Size)
         {
            int max = ((Size) a).max();
            if (max > 0)
               return max;
         }
      }
      return super.getMaxlength();
   }


   @Override
   public int getSize()
   {
      Annotation[] annotations = ELTools.readAnnotations(this);
      for (Annotation a : annotations)
      {
         if (a instanceof Size)
         {
            int max = ((Size) a).max();
            if (max > 0)
               return max;
         }
      }
      return super.getSize();
   }

   @Override
   public void processEvent(SystemEvent event) throws AbortProcessingException
   {
      if (!FacesContext.getCurrentInstance().isPostback())
      {
         insertLabelBeforeThisInputField();
         insertMessageBehindThisInputField();
      }
   }

   private void insertMessageBehindThisInputField()
   {
      NGMessage l = new NGMessage();
      l.setFor(getId());
      l.setDisplay("text");
      l.setTarget(this);
      List<UIComponent> tree = getParent().getChildren();
      for (int i = 0; i < tree.size(); i++)
      {
         if (tree.get(i) == this)
         {
            tree.add(i + 1, l);
            break;
         }
      }
   }

   private void insertLabelBeforeThisInputField()
   {
      OutputLabel l = new OutputLabel();
      l.setFor(getId());
      l.setValue(getLabel());
      List<UIComponent> tree = getParent().getChildren();
      for (int i = 0; i < tree.size(); i++)
      {
         if (tree.get(i) == this)
         {
            tree.add(i, l);
            break;
         }
      }
   }

   @Override
   public boolean isListenerForSource(Object source)
   {
      return (source instanceof UIViewRoot);
   }
}
