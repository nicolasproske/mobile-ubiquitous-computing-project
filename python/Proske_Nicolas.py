import paho.mqtt.client as mqtt
from tkinter import *

# Define width and height
canvas_width = 600
canvas_height = canvas_width

circle = 0

# =-----------= #
#    Tkinter
# =-----------= #

master = Tk() # Init Tkinter
master.title('Studienarbeit MAUC, MQTT Steuerung') # Set window title
master.config(cursor='none') # Hide cursor

# Define canvas
w = Canvas(master,
           width=canvas_width,
           height=canvas_height,
           background='white')

w.pack(expand=YES, fill=BOTH) # Set canvas to full width and height

# Define label to display received data later on
message = Label(master, text='Init') # Init label to display score later on
message.pack(side=BOTTOM)  # Placing the label at the bottom

# =-----------= #
#     MQTT
# =-----------= #

# Define broker and topics
broker = '127.0.0.1'
sub_topic = 'StA/message'  # Receive messages on this topic
pub_topic = 'StA/data'  # Send messages to this topic


# Connecting to mqtt
def on_connect(client, userdata, flags, rc):
    print('Connected with result code ' + str(rc))
    client.subscribe(sub_topic)


# Receiving a mqtt message
def on_message(client, userdata, msg):
    global message
    message.config(text="Total " + str(msg.payload, 'utf-8'))
    print('Received from ' + sub_topic + ': ' + str(msg.payload, 'utf-8'))


# Sending a message to specific topic
def on_publish(client, topic, msg):
    client.publish(pub_topic, msg)
    print('Published to ' + topic + ': ' + msg)


# Finally connecting to broker
client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message
client.connect(broker, 1883, 60)

client.loop_start()


# =-----------= #
#    Tkinter
# =-----------= #

# Draw circle at mouse position
def paint(event):
    global circle
    w.delete(circle)  # Remove existing circle

    x, y = event.x + 3, event.y + 7  # Get center of x and y coordinated of the mouse pointer

    radius = 10  # Define size of the circle

    # Draw new circle
    circle = w.create_oval(x + radius, y + radius, x - radius, y - radius, outline='black', fill='red')

    # Publish current mouse position
    # Get centered value in pixel
    center = canvas_width / 2

    # Multiplier to calculate the value between -4.905 to 4.905 from current pixel-location
    mult = 4.905 / center

    x_new = (x - center) * -mult
    y_new = (y - center) * mult
    
    # Publish values to broker
    on_publish(client, pub_topic, str(x_new) + ',' + str(y_new))


w.bind('<Motion>', paint)
mainloop()
