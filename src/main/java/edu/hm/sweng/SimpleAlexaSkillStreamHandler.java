package edu.hm.sweng;

import com.amazon.ask.SkillStreamHandler;
import com.amazon.ask.Skills;
import edu.hm.sweng.handlers.AddDescriptionIntentHandler;
import edu.hm.sweng.handlers.CheckoutRemindersIntentHandler;
import edu.hm.sweng.handlers.ConfirmReminderIntentHandler;
import edu.hm.sweng.handlers.ConnectToIntentHandler;
import edu.hm.sweng.handlers.CreateReminderIntentHandler;
import edu.hm.sweng.handlers.CustomLaunchRequestHandler;
import edu.hm.sweng.handlers.DeleteReminderIntentHandler;
import edu.hm.sweng.handlers.FallbackIntentHandler;
import edu.hm.sweng.handlers.HelpIntentHandler;
import edu.hm.sweng.handlers.NoIntentHandler;
import edu.hm.sweng.handlers.ReadDescriptionIntentHandler;
import edu.hm.sweng.handlers.ReadReminderIntentHandler;
import edu.hm.sweng.handlers.RecurringReminderIntentHandler;
import edu.hm.sweng.handlers.ToggleSharingRemindersIntentHandler;
import edu.hm.sweng.handlers.UpdateDescriptionIntentHandler;
import edu.hm.sweng.handlers.UpdateReminderIntentHandler;

import static edu.hm.sweng.model.ReminderController.getReminderController;

@SuppressWarnings("checkstyle:ClassDataAbstractionCoupling")
public class SimpleAlexaSkillStreamHandler extends SkillStreamHandler {

    public SimpleAlexaSkillStreamHandler() {
        super(Skills.standard()
                  .addRequestHandler(new CustomLaunchRequestHandler())
                  .addRequestHandler(new HelpIntentHandler())
                  .addRequestHandler(new CreateReminderIntentHandler(getReminderController()))
                  .addRequestHandler(new AddDescriptionIntentHandler(getReminderController()))
                  .addRequestHandler(new NoIntentHandler(getReminderController()))
                  .addRequestHandler(new DeleteReminderIntentHandler(getReminderController()))
                  .addRequestHandler(new UpdateReminderIntentHandler(getReminderController()))
                  .addRequestHandler(new UpdateDescriptionIntentHandler(getReminderController()))
                  .addRequestHandler(new ReadReminderIntentHandler(getReminderController()))
                  .addRequestHandler(new ReadDescriptionIntentHandler(getReminderController()))
                  .addRequestHandler(new RecurringReminderIntentHandler(getReminderController()))
                  .addRequestHandler(new ConfirmReminderIntentHandler(getReminderController()))
                  .addRequestHandler(new FallbackIntentHandler())
                  .addRequestHandler(new ToggleSharingRemindersIntentHandler())
                  .addRequestHandler(new ConnectToIntentHandler())
                  .addRequestHandler(new CheckoutRemindersIntentHandler())
                  .withTableName("my-memo")
                  .withAutoCreateTable(true)
                  .build()
        );
    }
}