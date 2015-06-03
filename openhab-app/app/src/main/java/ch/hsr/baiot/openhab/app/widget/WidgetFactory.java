package ch.hsr.baiot.openhab.app.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.app.adapter.RefreshScheduler;
import ch.hsr.baiot.openhab.app.adapter.WidgetListAdapter;
import ch.hsr.baiot.openhab.sdk.model.Widget;

/**
 * Created by dominik on 01.06.15.
 */
public class WidgetFactory {

    public static final int TYPE_FRAME = 0;
    public static final int TYPE_GROUP = 1;
    public static final int TYPE_TEXT = 2;
    public static final int TYPE_SWITCH = 3;
    public static final int TYPE_WEBVIEW = 4;


    public static int getType(Widget widget) {
        if("Frame".equals(widget.type)) return TYPE_FRAME;
        else if("Group".equals(widget.type)) return TYPE_GROUP;
        else if("Switch".equals(widget.type)) return TYPE_SWITCH;
        else if("Webview".equals(widget.type)) return TYPE_WEBVIEW;
        else return TYPE_TEXT;
    }

    public static WidgetViewHolder createViewHolder(int type, ViewGroup viewGroup, WidgetListAdapter.OnWidgetListActionListener listener) {
        ViewGroup container = null;
        WidgetViewHolder vh = null;
        switch(type) {
            case TYPE_FRAME:
                container = (ViewGroup) LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.widget_frame, viewGroup, false);
                vh = new WidgetFrame(container, listener);
                break;
            case TYPE_GROUP:
                container = (ViewGroup) LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.widget_group, viewGroup, false);
                vh = new WidgetGroup(container, listener);
                break;
            case TYPE_SWITCH:
                container = (ViewGroup) LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.widget_switch, viewGroup, false);
                vh = new WidgetSwitch(container, listener);
                break;
            case TYPE_WEBVIEW:
                container = (ViewGroup) LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.widget_webview, viewGroup, false);
                vh = new WidgetWebview(container);
                break;
            default:
                container = (ViewGroup) LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.widget_text, viewGroup, false);
                vh = new WidgetText(container, listener);
                break;
        }
        return vh;
    }

    public static void bindViewHolder(WidgetViewHolder viewHolder, RefreshScheduler scheduler) {

        int type = getType(viewHolder.widget);

        switch(type) {
            case TYPE_FRAME:
                bind((WidgetFrame) viewHolder);
                break;
            case TYPE_GROUP:
                bind((WidgetGroup) viewHolder);
                break;
            case TYPE_SWITCH:
                bind((WidgetSwitch) viewHolder);
                break;
            case TYPE_TEXT:
                bind((WidgetText) viewHolder);
                break;
            case TYPE_WEBVIEW:
                ((WidgetWebview) viewHolder).webView.loadUrl("http://192.168.1.15:80/jpg/1/image.jpg");
                scheduler.register((WidgetWebview) viewHolder);
                break;
            default:
                break;
        }
    }


    public static void bind(WidgetFrame viewHolder) {
        viewHolder.textView.setText(viewHolder.widget.label);
    }

    public static void bind(WidgetGroup viewHolder) {
        viewHolder.imageView.setImageDrawable(getIcon(viewHolder.itemView.getContext(),
                viewHolder.widget.icon,
                getType(viewHolder.widget)));
        if(assignMappedValue(viewHolder.widget.label, viewHolder.titleView, viewHolder.subtitleView)) return;
        viewHolder.titleView.setText(viewHolder.widget.label);
        viewHolder.subtitleView.setText(viewHolder.widget.item.state);
    }

    public static void bind(WidgetSwitch viewHolder) {
        viewHolder.switchView.setChecked("ON".equals(viewHolder.widget.item.state));
        viewHolder.icon.setImageDrawable(getIcon(viewHolder.itemView.getContext(),
                viewHolder.widget.icon,
                getType(viewHolder.widget)));

        if(assignMappedValue(viewHolder.widget.label, viewHolder.textView, viewHolder.detailTextView)) return;
        viewHolder.textView.setText(viewHolder.widget.label);
        viewHolder.detailTextView.setText(viewHolder.widget.item.state);
    }

    public static void bind(WidgetText viewHolder) {
        String iconName = viewHolder.widget.icon;
        if("siren".equals(iconName)) iconName = viewHolder.widget.item.state;

        viewHolder.icon.setImageDrawable(getIcon(viewHolder.itemView.getContext(),
                iconName,
                getType(viewHolder.widget)));

        if(assignMappedValue(viewHolder.widget.label, viewHolder.textView, viewHolder.detailTextView)) return;

        viewHolder.textView.setText(viewHolder.widget.label);
        viewHolder.detailTextView.setText(viewHolder.widget.item.state);

    }

    public static boolean assignMappedValue(String label, TextView textView, TextView detailView) {
        int start = label.indexOf('[');
        int end = label.indexOf(']');
        if(start >= 0 && end >= 0) {
            String text = label.substring(0, start);
            textView.setText(text);
            detailView.setText(label.substring(start + 1, end));
            return true;
        } else {
            return false;
        }
    }

    public static Drawable getIcon(Context context, String iconName, int type) {
        if(iconName == null) return context.getResources().getDrawable(R.drawable.icon_dummy, context.getTheme());


        if("light-on".equals(iconName) || "switch-on".equals(iconName)) {
            if(type == TYPE_GROUP) {
                return context.getResources().getDrawable(R.drawable.ic_switch_on_square, context.getTheme());
            } else {
                return context.getResources().getDrawable(R.drawable.ic_switch_on, context.getTheme());
            }

        } else if("light-off".equals(iconName) || "switch-off".equals(iconName)) {
            if(type == TYPE_GROUP) {
                return context.getResources().getDrawable(R.drawable.ic_switch_off_square, context.getTheme());
            } else {
                return context.getResources().getDrawable(R.drawable.ic_switch_off, context.getTheme());
            }
        } else if("contact-true".equals(iconName)) {
            if(type == TYPE_GROUP) {
                return context.getResources().getDrawable(R.drawable.ic_window_open_square, context.getTheme());
            } else {
                return context.getResources().getDrawable(R.drawable.ic_window_open, context.getTheme());
            }
        } else if("contact-false".equals(iconName)) {
            if(type == TYPE_GROUP) {
                return context.getResources().getDrawable(R.drawable.ic_window_closed_square, context.getTheme());
            } else {
                return context.getResources().getDrawable(R.drawable.ic_window_closed, context.getTheme());
            }
        } else if("motion-on".equals(iconName)) {
            if(type == TYPE_GROUP) {
                return context.getResources().getDrawable(R.drawable.ic_motion_on_square, context.getTheme());
            } else {
                return context.getResources().getDrawable(R.drawable.ic_motion_on, context.getTheme());
            }
        } else if("motion-off".equals(iconName)) {
            if(type == TYPE_GROUP) {
                return context.getResources().getDrawable(R.drawable.ic_motion_off_square, context.getTheme());
            } else {
                return context.getResources().getDrawable(R.drawable.ic_motion_off, context.getTheme());
            }
        } else if("siren-on".equals(iconName)) {
            if(type == TYPE_GROUP) {
                return context.getResources().getDrawable(R.drawable.ic_alarm_on_square, context.getTheme());
            } else {
                return context.getResources().getDrawable(R.drawable.ic_alarm_on, context.getTheme());
            }
        } else if("siren".equals(iconName)) {
            if(type == TYPE_GROUP) {
                return context.getResources().getDrawable(R.drawable.ic_alarm_off_square, context.getTheme());
            } else {
                return context.getResources().getDrawable(R.drawable.ic_alarm_off, context.getTheme());
            }
        } else if("house".equals(iconName)) {
            if(type == TYPE_GROUP) {
                return context.getResources().getDrawable(R.drawable.ic_camera_on_square, context.getTheme());
            } else {
                return context.getResources().getDrawable(R.drawable.ic_camera_on, context.getTheme());
            }
        } else if("house-off".equals(iconName)) {
            if(type == TYPE_GROUP) {
                return context.getResources().getDrawable(R.drawable.ic_camera_off_square, context.getTheme());
            } else {
                return context.getResources().getDrawable(R.drawable.ic_camera_off, context.getTheme());
            }
        }
        else if("ruhig".equals(iconName)) {
             return context.getResources().getDrawable(R.drawable.ic_motion_off, context.getTheme());
        }
        else if(iconName.contains("Sabotage") || iconName.contains("Bewegung")) {
            return context.getResources().getDrawable(R.drawable.ic_motion_on, context.getTheme());
        }
        return context.getResources().getDrawable(R.drawable.icon_dummy, context.getTheme());

    }


}
