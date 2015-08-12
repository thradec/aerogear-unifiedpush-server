using AeroGear.Push;

protected async override void OnLaunched(LaunchActivatedEventArgs e)
{
  await FHClient.Init();
  await FHClient.RegisterPush(HandleNotification);

  ...
}

void HandleNotification(object sender, PushReceivedEvent e)
{
  Debug.WriteLine("notification received {0}", e.Args.message);
}
